package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.common.utils.OrderNoGenerator;
import com.buildflow.erp.entity.PurchaseOrder;
import com.buildflow.erp.entity.PurchaseOrderItem;
import com.buildflow.erp.entity.CrmSupplier;
import com.buildflow.erp.mapper.PurchaseOrderItemMapper;
import com.buildflow.erp.mapper.PurchaseOrderMapper;
import com.buildflow.erp.mapper.CrmSupplierMapper;
import com.buildflow.erp.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 采购订单服务实现类
 * 实现采购订单管理的核心业务逻辑，包括分页查询、新增采购订单（含明细）、审核采购订单、作废采购订单等操作。
 * 新增采购订单时自动生成单号并计算明细金额和订单总金额。
 */
@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Autowired
    private CrmSupplierMapper crmSupplierMapper;

    /**
     * 分页查询采购订单列表
     * 支持按采购订单号模糊搜索、供应商过滤、单据状态过滤，按创建时间倒序排列
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param orderNo     采购订单号（模糊查询），可为null
     * @param supplierId  供应商ID，可为null表示不过滤
     * @param status      单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页采购订单列表
     */
    @Override
    public R<PageResult<PurchaseOrder>> page(Integer pageNum, Integer pageSize, String orderNo, Long supplierId, String status) {
        Page<PurchaseOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), PurchaseOrder::getOrderNo, orderNo)
                .eq(supplierId != null, PurchaseOrder::getSupplierId, supplierId)
                .eq(StringUtils.hasText(status), PurchaseOrder::getStatus, status)
                .orderByDesc(PurchaseOrder::getCreateTime);
        Page<PurchaseOrder> result = purchaseOrderMapper.selectPage(page, wrapper);
        List<PurchaseOrder> records = result.getRecords();
        // 批量查询供应商名称并填充到结果中
        if (!records.isEmpty()) {
            Set<Long> supplierIds = records.stream()
                    .map(PurchaseOrder::getSupplierId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!supplierIds.isEmpty()) {
                List<CrmSupplier> suppliers = crmSupplierMapper.selectBatchIds(supplierIds);
                Map<Long, String> supplierMap = suppliers.stream()
                        .collect(Collectors.toMap(CrmSupplier::getId, CrmSupplier::getName));
                records.forEach(o -> o.setSupplierName(supplierMap.get(o.getSupplierId())));
            }
        }
        return R.ok(new PageResult<>(result.getTotal(), records));
    }

    /**
     * 新增采购订单（含明细）
     * 自动生成采购订单号（PO+日期+流水号），设置初始状态为草稿，
     * 自动计算每条明细金额（unitPrice × quantity）和订单总金额，插入主表和明细表
     *
     * @param order 采购订单主表对象，需包含supplierId等字段
     * @param items 采购订单明细列表，每条需包含productId、unitPrice、quantity
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> add(PurchaseOrder order, List<PurchaseOrderItem> items) {
        // 生成采购订单号并设置初始状态
        order.setOrderNo(OrderNoGenerator.generate(OrderNoGenerator.PURCHASE_ORDER));
        order.setStatus(Constants.ORDER_STATUS_DRAFT);

        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 插入采购订单明细，计算每条明细金额
        if (items != null && !items.isEmpty()) {
            for (PurchaseOrderItem item : items) {
                // 计算明细金额 = 单价 × 数量
                BigDecimal amount = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
                item.setAmount(amount);
                totalAmount = totalAmount.add(amount);
            }
        }
        order.setTotalAmount(totalAmount);

        // 插入采购订单主表
        purchaseOrderMapper.insert(order);

        // 插入采购订单明细
        if (items != null && !items.isEmpty()) {
            for (PurchaseOrderItem item : items) {
                item.setOrderId(order.getId());
                purchaseOrderItemMapper.insert(item);
            }
        }
        return R.ok();
    }

    /**
     * 审核采购订单
     * 校验单据状态为草稿，更新状态为已审核，记录审核人和审核时间
     *
     * @param id      采购订单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     */
    @Override
    public R<Void> audit(Long id, Long auditBy) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            return R.fail("采购订单不存在");
        }
        if (!Constants.ORDER_STATUS_DRAFT.equals(order.getStatus())) {
            return R.fail("采购订单状态不允许审核");
        }

        // 更新采购订单状态为已审核
        LambdaUpdateWrapper<PurchaseOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PurchaseOrder::getId, id)
                .set(PurchaseOrder::getStatus, Constants.ORDER_STATUS_AUDITED)
                .set(PurchaseOrder::getAuditBy, auditBy)
                .set(PurchaseOrder::getAuditTime, LocalDateTime.now());
        purchaseOrderMapper.update(null, updateWrapper);

        return R.ok();
    }

    /**
     * 作废采购订单
     * 仅草稿状态的采购订单可作废
     *
     * @param id 采购订单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    @Override
    public R<Void> voidOrder(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            return R.fail("采购订单不存在");
        }
        if (Constants.ORDER_STATUS_VOID.equals(order.getStatus())) {
            return R.fail("采购订单已作废，不可重复操作");
        }

        // 更新采购订单状态为已作废
        LambdaUpdateWrapper<PurchaseOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PurchaseOrder::getId, id)
                .set(PurchaseOrder::getStatus, Constants.ORDER_STATUS_VOID);
        purchaseOrderMapper.update(null, updateWrapper);

        return R.ok();
    }
}
