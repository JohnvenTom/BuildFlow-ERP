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
import com.buildflow.erp.entity.DeliveryOrderItem;
import com.buildflow.erp.entity.SalesOrder;
import com.buildflow.erp.entity.SalesOrderItem;
import com.buildflow.erp.entity.WmsWarehouse;
import com.buildflow.erp.mapper.CrmCustomerMapper;
import com.buildflow.erp.mapper.DeliveryOrderItemMapper;
import com.buildflow.erp.mapper.DeliveryOrderMapper;
import com.buildflow.erp.mapper.SalesOrderItemMapper;
import com.buildflow.erp.mapper.SalesOrderMapper;
import com.buildflow.erp.mapper.WmsWarehouseMapper;
import com.buildflow.erp.service.DeliveryOrderService;
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
 * 工地出库单服务实现类
 * 实现工地分次出库管理的核心业务逻辑，包括分页查询、新增出库单（含明细）、审核出库单、作废出库单等操作。
 * 新增出库单时校验出库数量不超过订单未发货数量，审核时通过事务扣减库存、累加订单明细已出库数量、增加客户欠款。
 */
@Service
public class DeliveryOrderServiceImpl implements DeliveryOrderService {

    @Autowired
    private DeliveryOrderMapper deliveryOrderMapper;

    @Autowired
    private DeliveryOrderItemMapper deliveryOrderItemMapper;

    @Autowired
    private SalesOrderMapper salesOrderMapper;

    @Autowired
    private SalesOrderItemMapper salesOrderItemMapper;

    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    @Autowired
    private WmsWarehouseMapper wmsWarehouseMapper;

    @Autowired
    private WmsInventoryService wmsInventoryService;

    /**
     * 分页查询出库单列表
     * 支持按出库单号模糊搜索、关联销售订单、单据状态过滤，按创建时间倒序排列
     *
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param deliveryNo 出库单号（模糊查询），可为null
     * @param orderId    关联销售订单ID，可为null表示不过滤
     * @param status     单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页出库单列表
     */
    @Override
    public R<PageResult<DeliveryOrder>> page(Integer pageNum, Integer pageSize, String deliveryNo, Long orderId, String status) {
        Page<DeliveryOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(deliveryNo), DeliveryOrder::getDeliveryNo, deliveryNo)
                .eq(orderId != null, DeliveryOrder::getOrderId, orderId)
                .eq(StringUtils.hasText(status), DeliveryOrder::getStatus, status)
                .orderByDesc(DeliveryOrder::getCreateTime);
        Page<DeliveryOrder> result = deliveryOrderMapper.selectPage(page, wrapper);
        List<DeliveryOrder> records = result.getRecords();
        // 批量查询销售订单号并填充到结果中
        List<Long> orderIds = records.stream()
                .map(DeliveryOrder::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!orderIds.isEmpty()) {
            List<SalesOrder> orders = salesOrderMapper.selectBatchIds(orderIds);
            Map<Long, String> orderMap = orders.stream()
                    .collect(Collectors.toMap(SalesOrder::getId, SalesOrder::getOrderNo));
            records.forEach(e -> e.setSalesOrderNo(orderMap.get(e.getOrderId())));
        }
        // 批量查询出库仓库名称并填充到结果中
        List<Long> warehouseIds = records.stream()
                .map(DeliveryOrder::getWarehouseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!warehouseIds.isEmpty()) {
            List<WmsWarehouse> warehouses = wmsWarehouseMapper.selectBatchIds(warehouseIds);
            Map<Long, String> warehouseMap = warehouses.stream()
                    .collect(Collectors.toMap(WmsWarehouse::getId, WmsWarehouse::getName));
            records.forEach(e -> e.setWarehouseName(warehouseMap.get(e.getWarehouseId())));
        }
        return R.ok(new PageResult<>(result.getTotal(), records));
    }

    /**
     * 新增出库单（含明细）
     * 自动生成出库单号（DO+日期+流水号），设置初始状态为草稿，
     * 校验每条出库明细数量不超过关联销售订单明细的未发货数量（订单明细quantity - deliveredQty），
     * 计算出库总金额，插入主表和明细表
     *
     * @param delivery 出库单主表对象，需包含orderId、warehouseId等字段
     * @param items    出库单明细列表，每条需包含productId、colorNo、quantity、unitPrice
     * @return 操作结果，出库数量超量或订单明细不存在时返回R.fail()
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> add(DeliveryOrder delivery, List<DeliveryOrderItem> items) {
        // 校验关联销售订单存在
        SalesOrder salesOrder = salesOrderMapper.selectById(delivery.getOrderId());
        if (salesOrder == null) {
            return R.fail("关联销售订单不存在");
        }

        // 查询销售订单明细，用于校验出库数量
        LambdaQueryWrapper<SalesOrderItem> orderItemWrapper = new LambdaQueryWrapper<>();
        orderItemWrapper.eq(SalesOrderItem::getOrderId, delivery.getOrderId());
        List<SalesOrderItem> orderItems = salesOrderItemMapper.selectList(orderItemWrapper);

        // 校验每条出库明细数量不超过订单未发货数量
        if (items != null && !items.isEmpty()) {
            for (DeliveryOrderItem item : items) {
                // 按 商品ID + 色号 匹配订单明细
                SalesOrderItem matchedOrderItem = null;
                for (SalesOrderItem orderItem : orderItems) {
                    if (orderItem.getProductId().equals(item.getProductId())
                            && (orderItem.getColorNo() == null ? item.getColorNo() == null
                                : orderItem.getColorNo().equals(item.getColorNo()))) {
                        matchedOrderItem = orderItem;
                        break;
                    }
                }
                if (matchedOrderItem == null) {
                    return R.fail("出库明细对应的订单明细不存在，商品ID：" + item.getProductId() + "，色号：" + item.getColorNo());
                }
                // 已出库数量为null时按0处理
                int deliveredQty = matchedOrderItem.getDeliveredQty() == null ? 0 : matchedOrderItem.getDeliveredQty();
                int undeliveredQty = matchedOrderItem.getQuantity() - deliveredQty;
                if (item.getQuantity() > undeliveredQty) {
                    return R.fail("出库数量超过订单未发货数量，商品ID：" + item.getProductId()
                            + "，色号：" + item.getColorNo()
                            + "，未发货数量：" + undeliveredQty
                            + "，本次出库数量：" + item.getQuantity());
                }
            }
        }

        // 生成出库单号并设置初始状态
        delivery.setDeliveryNo(OrderNoGenerator.generate(OrderNoGenerator.DELIVERY));
        delivery.setStatus(Constants.ORDER_STATUS_DRAFT);

        // 计算出库总金额 = sum(单价 × 数量)
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (items != null && !items.isEmpty()) {
            for (DeliveryOrderItem item : items) {
                totalAmount = totalAmount.add(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
            }
        }
        delivery.setTotalAmount(totalAmount);

        // 插入出库单主表
        deliveryOrderMapper.insert(delivery);

        // 插入出库单明细
        if (items != null && !items.isEmpty()) {
            for (DeliveryOrderItem item : items) {
                item.setDeliveryId(delivery.getId());
                deliveryOrderItemMapper.insert(item);
            }
        }
        return R.ok();
    }

    /**
     * 审核出库单
     * 事务操作：校验单据状态为草稿，逐条明细调用decreaseStock扣减库存，
     * 更新关联销售订单明细已出库数量，增加客户当前欠款，更新状态为已审核，生成客户应收台账（预留）。
     * 注意：客户应收台账生成功能待FinReceivableService完成后集成。
     *
     * @param id      出库单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     * @throws com.buildflow.erp.common.exception.BusinessException 库存不足时抛出，事务回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> audit(Long id, Long auditBy) {
        DeliveryOrder delivery = deliveryOrderMapper.selectById(id);
        if (delivery == null) {
            return R.fail("出库单不存在");
        }
        if (!Constants.ORDER_STATUS_DRAFT.equals(delivery.getStatus())) {
            return R.fail("出库单状态不允许审核");
        }

        // 查询出库单明细
        LambdaQueryWrapper<DeliveryOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(DeliveryOrderItem::getDeliveryId, id);
        List<DeliveryOrderItem> items = deliveryOrderItemMapper.selectList(itemWrapper);

        // 逐条明细扣减库存（库存不足将抛出BusinessException，触发事务回滚）
        for (DeliveryOrderItem item : items) {
            wmsInventoryService.decreaseStock(item.getProductId(), delivery.getWarehouseId(),
                    null, item.getColorNo(), item.getQuantity());
        }

        // 更新关联销售订单明细的已出库数量
        for (DeliveryOrderItem item : items) {
            // 按 商品ID + 色号 匹配订单明细
            LambdaQueryWrapper<SalesOrderItem> orderItemWrapper = new LambdaQueryWrapper<>();
            orderItemWrapper.eq(SalesOrderItem::getOrderId, delivery.getOrderId())
                    .eq(SalesOrderItem::getProductId, item.getProductId())
                    .eq(item.getColorNo() != null, SalesOrderItem::getColorNo, item.getColorNo())
                    .isNull(item.getColorNo() == null, SalesOrderItem::getColorNo);
            SalesOrderItem orderItem = salesOrderItemMapper.selectOne(orderItemWrapper);
            if (orderItem != null) {
                int deliveredQty = (orderItem.getDeliveredQty() == null ? 0 : orderItem.getDeliveredQty()) + item.getQuantity();
                LambdaUpdateWrapper<SalesOrderItem> updateItemWrapper = new LambdaUpdateWrapper<>();
                updateItemWrapper.eq(SalesOrderItem::getId, orderItem.getId())
                        .set(SalesOrderItem::getDeliveredQty, deliveredQty);
                salesOrderItemMapper.update(null, updateItemWrapper);
            }
        }

        // 增加客户当前欠款：通过销售订单获取客户ID
        SalesOrder salesOrder = salesOrderMapper.selectById(delivery.getOrderId());
        if (salesOrder != null && salesOrder.getCustomerId() != null) {
            CrmCustomer customer = crmCustomerMapper.selectById(salesOrder.getCustomerId());
            if (customer != null) {
                BigDecimal currentDebt = customer.getCurrentDebt() == null ? BigDecimal.ZERO : customer.getCurrentDebt();
                LambdaUpdateWrapper<CrmCustomer> customerWrapper = new LambdaUpdateWrapper<>();
                customerWrapper.eq(CrmCustomer::getId, customer.getId())
                        .set(CrmCustomer::getCurrentDebt, currentDebt.add(delivery.getTotalAmount()));
                crmCustomerMapper.update(null, customerWrapper);
            }
        }

        // 更新出库单状态为已审核
        LambdaUpdateWrapper<DeliveryOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DeliveryOrder::getId, id)
                .set(DeliveryOrder::getStatus, Constants.ORDER_STATUS_AUDITED)
                .set(DeliveryOrder::getAuditBy, auditBy)
                .set(DeliveryOrder::getAuditTime, LocalDateTime.now());
        deliveryOrderMapper.update(null, updateWrapper);

        // TODO: 待FinReceivableService完成后，生成客户应收台账
        // finReceivableService.createReceivable(salesOrder.getCustomerId(), delivery.getDeliveryNo(), delivery.getTotalAmount());

        return R.ok();
    }

    /**
     * 作废出库单
     * 仅草稿状态的出库单可作废
     *
     * @param id 出库单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    @Override
    public R<Void> voidOrder(Long id) {
        DeliveryOrder delivery = deliveryOrderMapper.selectById(id);
        if (delivery == null) {
            return R.fail("出库单不存在");
        }
        if (Constants.ORDER_STATUS_VOID.equals(delivery.getStatus())) {
            return R.fail("出库单已作废，不可重复操作");
        }

        // 更新出库单状态为已作废
        LambdaUpdateWrapper<DeliveryOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DeliveryOrder::getId, id)
                .set(DeliveryOrder::getStatus, Constants.ORDER_STATUS_VOID);
        deliveryOrderMapper.update(null, updateWrapper);

        return R.ok();
    }
}
