package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.dto.DeliveryOrderDTO;
import com.buildflow.erp.entity.DeliveryOrder;
import com.buildflow.erp.service.DeliveryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 工地出库单管理控制器
 * 提供工地分次出库单的分页查询、新增（含明细）、审核、作废等接口
 */
@RestController
@RequestMapping("/sales/delivery")
public class DeliveryOrderController {

    @Autowired
    private DeliveryOrderService deliveryOrderService;

    /**
     * 分页查询出库单列表
     * 支持按出库单号模糊搜索、关联销售订单、单据状态过滤
     *
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param deliveryNo 出库单号（模糊查询），可为空
     * @param orderId    关联销售订单ID，可为空
     * @param status     单据状态（0-草稿 1-已审核 2-已作废），可为空
     * @return 分页结果，包含总记录数和当前页出库单列表
     */
    @GetMapping("/page")
    public R<PageResult<DeliveryOrder>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String deliveryNo,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status) {
        return deliveryOrderService.page(pageNum, pageSize, deliveryNo, orderId, status);
    }

    /**
     * 新增出库单（含明细）
     * 自动生成出库单号，设置初始状态为草稿，校验出库数量不超过订单未发货数量
     *
     * @param dto 出库单请求DTO，包含delivery（出库单主表）和items（明细列表）
     * @return 操作结果
     */
    @PostMapping
    public R<Void> add(@RequestBody DeliveryOrderDTO dto) {
        return deliveryOrderService.add(dto.getDelivery(), dto.getItems());
    }

    /**
     * 审核出库单
     * 事务操作：扣减库存、累加订单明细已出库数量、增加客户欠款、生成客户应收台账（预留）
     *
     * @param id      出库单ID
     * @param auditBy 审核人ID
     * @return 操作结果
     */
    @PutMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id, @RequestParam Long auditBy) {
        return deliveryOrderService.audit(id, auditBy);
    }

    /**
     * 作废出库单
     * 更新单据状态为已作废
     *
     * @param id 出库单ID
     * @return 操作结果
     */
    @PutMapping("/void/{id}")
    public R<Void> voidOrder(@PathVariable Long id) {
        return deliveryOrderService.voidOrder(id);
    }
}
