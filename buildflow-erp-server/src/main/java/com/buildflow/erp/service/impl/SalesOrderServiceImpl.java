package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.common.utils.OrderNoGenerator;
import com.buildflow.erp.entity.SalesOrder;
import com.buildflow.erp.entity.SalesOrderItem;
import com.buildflow.erp.mapper.SalesOrderItemMapper;
import com.buildflow.erp.mapper.SalesOrderMapper;
import com.buildflow.erp.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售订单服务实现类
 * 实现销售订单管理的核心业务逻辑，包括分页查询、订单详情（含明细）、新增销售订单（含明细）、审核、作废等操作。
 * 新增销售订单时自动生成单号、计算明细金额、订单总金额及提成金额。
 */
@Service
public class SalesOrderServiceImpl implements SalesOrderService {

    @Autowired
    private SalesOrderMapper salesOrderMapper;

    @Autowired
    private SalesOrderItemMapper salesOrderItemMapper;

    /**
     * 分页查询销售订单列表
     * 支持按销售订单号模糊搜索、客户、业务员、单据状态过滤，按创建时间倒序排列
     *
     * @param pageNum       当前页码，从1开始
     * @param pageSize      每页记录数
     * @param orderNo       销售订单号（模糊查询），可为null
     * @param customerId    客户ID，可为null表示不过滤
     * @param salespersonId 业务员ID，可为null表示不过滤
     * @param status        单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页销售订单列表
     */
    @Override
    public R<PageResult<SalesOrder>> page(Integer pageNum, Integer pageSize, String orderNo, Long customerId, Long salespersonId, String status) {
        Page<SalesOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), SalesOrder::getOrderNo, orderNo)
                .eq(customerId != null, SalesOrder::getCustomerId, customerId)
                .eq(salespersonId != null, SalesOrder::getSalespersonId, salespersonId)
                .eq(StringUtils.hasText(status), SalesOrder::getStatus, status)
                .orderByDesc(SalesOrder::getCreateTime);
        Page<SalesOrder> result = salesOrderMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    /**
     * 查询销售订单详情（含明细列表）
     * 根据订单ID查询主表信息，并查询关联的明细列表一并设置到主表的items字段返回
     *
     * @param id 销售订单ID
     * @return 销售订单主表对象（含明细列表），订单不存在时返回R.fail()
     */
    @Override
    public R<SalesOrder> detail(Long id) {
        SalesOrder order = salesOrderMapper.selectById(id);
        if (order == null) {
            return R.fail("销售订单不存在");
        }
        // 查询关联明细列表
        LambdaQueryWrapper<SalesOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(SalesOrderItem::getOrderId, id);
        List<SalesOrderItem> items = salesOrderItemMapper.selectList(itemWrapper);
        order.setItems(items);
        return R.ok(order);
    }

    /**
     * 新增销售订单（含明细）
     * 自动生成销售订单号（SO+日期+流水号），设置初始状态为草稿，
     * 自动计算每条明细金额（unitPrice × quantity × discount，discount为null时按1处理）、
     * 订单总金额及提成金额（totalAmount × commissionRate，commissionRate为null时提成金额为0），插入主表和明细表
     *
     * @param order 销售订单主表对象，需包含customerId、salespersonId、commissionRate等字段
     * @param items 销售订单明细列表，每条需包含productId、unitPrice、quantity、discount
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> add(SalesOrder order, List<SalesOrderItem> items) {
        // 生成销售订单号并设置初始状态
        order.setOrderNo(OrderNoGenerator.generate(OrderNoGenerator.SALES_ORDER));
        order.setStatus(Constants.ORDER_STATUS_DRAFT);

        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 计算每条明细金额并累加总金额
        if (items != null && !items.isEmpty()) {
            for (SalesOrderItem item : items) {
                // 折扣率为null时按1处理（不打折）
                BigDecimal discount = item.getDiscount() == null ? BigDecimal.ONE : item.getDiscount();
                // 明细金额 = 单价 × 数量 × 折扣率
                BigDecimal amount = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())).multiply(discount);
                item.setAmount(amount);
                // 初始化已出库数量为0
                item.setDeliveredQty(0);
                totalAmount = totalAmount.add(amount);
            }
        }
        order.setTotalAmount(totalAmount);

        // 计算提成金额 = 总金额 × 提成比例（提成比例为null时提成金额为0）
        BigDecimal commissionRate = order.getCommissionRate();
        if (commissionRate != null) {
            order.setCommissionAmount(totalAmount.multiply(commissionRate));
        } else {
            order.setCommissionAmount(BigDecimal.ZERO);
        }

        // 插入销售订单主表
        salesOrderMapper.insert(order);

        // 插入销售订单明细
        if (items != null && !items.isEmpty()) {
            for (SalesOrderItem item : items) {
                item.setOrderId(order.getId());
                salesOrderItemMapper.insert(item);
            }
        }
        return R.ok();
    }

    /**
     * 审核销售订单
     * 校验单据状态为草稿，更新状态为已审核，记录审核人和审核时间
     *
     * @param id      销售订单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     */
    @Override
    public R<Void> audit(Long id, Long auditBy) {
        SalesOrder order = salesOrderMapper.selectById(id);
        if (order == null) {
            return R.fail("销售订单不存在");
        }
        if (!Constants.ORDER_STATUS_DRAFT.equals(order.getStatus())) {
            return R.fail("销售订单状态不允许审核");
        }

        // 更新销售订单状态为已审核
        LambdaUpdateWrapper<SalesOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SalesOrder::getId, id)
                .set(SalesOrder::getStatus, Constants.ORDER_STATUS_AUDITED)
                .set(SalesOrder::getAuditBy, auditBy)
                .set(SalesOrder::getAuditTime, LocalDateTime.now());
        salesOrderMapper.update(null, updateWrapper);

        return R.ok();
    }

    /**
     * 作废销售订单
     * 仅草稿状态的销售订单可作废
     *
     * @param id 销售订单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    @Override
    public R<Void> voidOrder(Long id) {
        SalesOrder order = salesOrderMapper.selectById(id);
        if (order == null) {
            return R.fail("销售订单不存在");
        }
        if (Constants.ORDER_STATUS_VOID.equals(order.getStatus())) {
            return R.fail("销售订单已作废，不可重复操作");
        }

        // 更新销售订单状态为已作废
        LambdaUpdateWrapper<SalesOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SalesOrder::getId, id)
                .set(SalesOrder::getStatus, Constants.ORDER_STATUS_VOID);
        salesOrderMapper.update(null, updateWrapper);

        return R.ok();
    }
}
