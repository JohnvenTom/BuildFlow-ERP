package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.DeliveryOrder;
import com.buildflow.erp.entity.DeliveryOrderItem;

import java.util.List;

/**
 * 工地出库单服务接口
 * 定义工地分次出库管理的核心业务方法，包括分页查询、新增出库单（含明细）、审核出库单、作废出库单等操作。
 * 审核出库单时通过事务扣减库存、累加订单明细已出库数量、增加客户欠款。
 */
public interface DeliveryOrderService {

    /**
     * 分页查询出库单列表
     * 支持按出库单号模糊搜索、关联销售订单、单据状态过滤
     *
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param deliveryNo 出库单号（模糊查询），可为null
     * @param orderId    关联销售订单ID，可为null表示不过滤
     * @param status     单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页出库单列表
     */
    R<PageResult<DeliveryOrder>> page(Integer pageNum, Integer pageSize, String deliveryNo, Long orderId, String status);

    /**
     * 新增出库单（含明细）
     * 自动生成出库单号（DO+日期+流水号），设置初始状态为草稿，
     * 校验每条出库明细数量不超过关联销售订单明细的未发货数量（quantity - deliveredQty），插入主表和明细表
     *
     * @param delivery 出库单主表对象，需包含orderId、warehouseId等字段
     * @param items    出库单明细列表，每条需包含productId、colorNo、quantity、unitPrice
     * @return 操作结果，出库数量超量时返回R.fail()
     */
    R<Void> add(DeliveryOrder delivery, List<DeliveryOrderItem> items);

    /**
     * 审核出库单
     * 事务操作：校验单据状态为草稿，逐条明细调用decreaseStock扣减库存，
     * 更新关联销售订单明细已出库数量，生成客户应收台账（预留），增加客户当前欠款，更新状态为已审核。
     * 注意：客户应收台账生成功能待FinReceivableService完成后集成。
     *
     * @param id      出库单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     * @throws com.buildflow.erp.common.exception.BusinessException 库存不足时抛出，事务回滚
     */
    R<Void> audit(Long id, Long auditBy);

    /**
     * 作废出库单
     * 仅草稿状态的出库单可作废
     *
     * @param id 出库单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    R<Void> voidOrder(Long id);
}
