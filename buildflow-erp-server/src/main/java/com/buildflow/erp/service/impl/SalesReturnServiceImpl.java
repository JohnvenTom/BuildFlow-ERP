package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.common.utils.OrderNoGenerator;
import com.buildflow.erp.entity.CrmCustomer;
import com.buildflow.erp.entity.DeliveryOrder;
import com.buildflow.erp.entity.SalesReturn;
import com.buildflow.erp.entity.SalesReturnItem;
import com.buildflow.erp.mapper.CrmCustomerMapper;
import com.buildflow.erp.mapper.DeliveryOrderMapper;
import com.buildflow.erp.mapper.SalesReturnItemMapper;
import com.buildflow.erp.mapper.SalesReturnMapper;
import com.buildflow.erp.service.SalesReturnService;
import com.buildflow.erp.service.WmsInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 销售退货单服务实现类
 * 实现销售退货管理的核心业务逻辑，包括分页查询、新增退货单（含明细）、审核退货单、作废退货单等操作。
 * 审核退货单时通过事务返还库存、减少客户欠款。
 */
@Service
public class SalesReturnServiceImpl implements SalesReturnService {

    @Autowired
    private SalesReturnMapper salesReturnMapper;

    @Autowired
    private SalesReturnItemMapper salesReturnItemMapper;

    @Autowired
    private DeliveryOrderMapper deliveryOrderMapper;

    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    @Autowired
    private WmsInventoryService wmsInventoryService;

    /**
     * 分页查询退货单列表
     * 支持按退货单号模糊搜索、客户、单据状态过滤，按创建时间倒序排列
     *
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param returnNo   退货单号（模糊查询），可为null
     * @param customerId 客户ID，可为null表示不过滤
     * @param status     单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页退货单列表
     */
    @Override
    public R<PageResult<SalesReturn>> page(Integer pageNum, Integer pageSize, String returnNo, Long customerId, String status) {
        Page<SalesReturn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SalesReturn> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(returnNo), SalesReturn::getReturnNo, returnNo)
                .eq(customerId != null, SalesReturn::getCustomerId, customerId)
                .eq(StringUtils.hasText(status), SalesReturn::getStatus, status)
                .orderByDesc(SalesReturn::getCreateTime);
        Page<SalesReturn> result = salesReturnMapper.selectPage(page, wrapper);
        List<SalesReturn> records = result.getRecords();
        // 批量查询客户名称并填充到结果中
        List<Long> customerIds = records.stream()
                .map(SalesReturn::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!customerIds.isEmpty()) {
            List<CrmCustomer> customers = crmCustomerMapper.selectBatchIds(customerIds);
            Map<Long, String> customerMap = customers.stream()
                    .collect(Collectors.toMap(CrmCustomer::getId, CrmCustomer::getName));
            records.forEach(e -> e.setCustomerName(customerMap.get(e.getCustomerId())));
        }
        // 批量查询关联出库单号并填充到结果中
        List<Long> deliveryIds = records.stream()
                .map(SalesReturn::getDeliveryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!deliveryIds.isEmpty()) {
            List<DeliveryOrder> deliveries = deliveryOrderMapper.selectBatchIds(deliveryIds);
            Map<Long, String> deliveryMap = deliveries.stream()
                    .collect(Collectors.toMap(DeliveryOrder::getId, DeliveryOrder::getDeliveryNo));
            records.forEach(e -> e.setDeliveryOrderNo(deliveryMap.get(e.getDeliveryId())));
        }
        return R.ok(new PageResult<>(result.getTotal(), records));
    }

    /**
     * 新增销售退货单（含明细）
     * 自动生成退货单号（SR+日期+流水号），设置初始状态为草稿，
     * 自动计算退货总金额（sum(单价 × 数量)），插入主表和明细表
     *
     * @param returnOrder 退货单主表对象，需包含customerId、deliveryId等字段
     * @param items       退货单明细列表，每条需包含productId、quantity、unitPrice
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> add(SalesReturn returnOrder, List<SalesReturnItem> items) {
        // 生成退货单号并设置初始状态
        returnOrder.setReturnNo(OrderNoGenerator.generate(OrderNoGenerator.SALES_RETURN));
        returnOrder.setStatus(Constants.ORDER_STATUS_DRAFT);

        // 计算退货总金额 = sum(单价 × 数量)
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (items != null && !items.isEmpty()) {
            for (SalesReturnItem item : items) {
                totalAmount = totalAmount.add(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
            }
        }
        returnOrder.setTotalAmount(totalAmount);

        // 插入退货单主表
        salesReturnMapper.insert(returnOrder);

        // 插入退货单明细
        if (items != null && !items.isEmpty()) {
            for (SalesReturnItem item : items) {
                item.setReturnId(returnOrder.getId());
                salesReturnItemMapper.insert(item);
            }
        }
        return R.ok();
    }

    /**
     * 审核退货单
     * 事务操作：校验单据状态为草稿，查询关联出库单获取仓库ID，
     * 逐条明细调用increaseStock返还库存，减少客户当前欠款，更新状态为已审核，减少客户应收台账（预留）。
     * 注意：客户应收台账减少功能待FinReceivableService完成后集成。
     *
     * @param id      退货单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在、状态非草稿或关联出库单不存在时返回R.fail()
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> audit(Long id, Long auditBy) {
        SalesReturn returnOrder = salesReturnMapper.selectById(id);
        if (returnOrder == null) {
            return R.fail("退货单不存在");
        }
        if (!Constants.ORDER_STATUS_DRAFT.equals(returnOrder.getStatus())) {
            return R.fail("退货单状态不允许审核");
        }

        // 查询关联出库单，获取仓库ID用于返还库存
        DeliveryOrder delivery = deliveryOrderMapper.selectById(returnOrder.getDeliveryId());
        if (delivery == null) {
            return R.fail("关联出库单不存在");
        }

        // 查询退货单明细
        LambdaQueryWrapper<SalesReturnItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(SalesReturnItem::getReturnId, id);
        List<SalesReturnItem> items = salesReturnItemMapper.selectList(itemWrapper);

        // 逐条明细返还库存（退货明细无色号字段，colorNo传null）
        for (SalesReturnItem item : items) {
            wmsInventoryService.increaseStock(item.getProductId(), delivery.getWarehouseId(),
                    null, null, item.getQuantity());
        }

        // 减少客户当前欠款
        if (returnOrder.getCustomerId() != null) {
            CrmCustomer customer = crmCustomerMapper.selectById(returnOrder.getCustomerId());
            if (customer != null) {
                BigDecimal currentDebt = customer.getCurrentDebt() == null ? BigDecimal.ZERO : customer.getCurrentDebt();
                // 欠款扣减退货金额，不为负数
                BigDecimal newDebt = currentDebt.subtract(returnOrder.getTotalAmount());
                if (newDebt.compareTo(BigDecimal.ZERO) < 0) {
                    newDebt = BigDecimal.ZERO;
                }
                LambdaUpdateWrapper<CrmCustomer> customerWrapper = new LambdaUpdateWrapper<>();
                customerWrapper.eq(CrmCustomer::getId, customer.getId())
                        .set(CrmCustomer::getCurrentDebt, newDebt);
                crmCustomerMapper.update(null, customerWrapper);
            }
        }

        // 更新退货单状态为已审核
        LambdaUpdateWrapper<SalesReturn> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SalesReturn::getId, id)
                .set(SalesReturn::getStatus, Constants.ORDER_STATUS_AUDITED)
                .set(SalesReturn::getAuditBy, auditBy)
                .set(SalesReturn::getAuditTime, LocalDateTime.now());
        salesReturnMapper.update(null, updateWrapper);

        // TODO: 待FinReceivableService完成后，减少客户应收台账
        // finReceivableService.reduceReceivable(returnOrder.getCustomerId(), returnOrder.getReturnNo(), returnOrder.getTotalAmount());

        return R.ok();
    }

    /**
     * 作废退货单
     * 仅草稿状态的退货单可作废
     *
     * @param id 退货单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    @Override
    public R<Void> voidOrder(Long id) {
        SalesReturn returnOrder = salesReturnMapper.selectById(id);
        if (returnOrder == null) {
            return R.fail("退货单不存在");
        }
        if (Constants.ORDER_STATUS_VOID.equals(returnOrder.getStatus())) {
            return R.fail("退货单已作废，不可重复操作");
        }

        // 更新退货单状态为已作废
        LambdaUpdateWrapper<SalesReturn> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SalesReturn::getId, id)
                .set(SalesReturn::getStatus, Constants.ORDER_STATUS_VOID);
        salesReturnMapper.update(null, updateWrapper);

        return R.ok();
    }
}
