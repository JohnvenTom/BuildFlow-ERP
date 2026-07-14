package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.dto.SalesOrderDTO;
import com.buildflow.erp.entity.SalesOrder;
import com.buildflow.erp.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 销售订单管理控制器
 * 提供销售订单的分页查询、详情查询、新增（含明细）、审核、作废等接口
 */
@RestController
@RequestMapping("/sales/order")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    /**
     * 分页查询销售订单列表
     * 支持按销售订单号模糊搜索、客户、业务员、单据状态过滤
     *
     * @param pageNum       当前页码，从1开始
     * @param pageSize      每页记录数
     * @param orderNo       销售订单号（模糊查询），可为空
     * @param customerId    客户ID，可为空
     * @param salespersonId 业务员ID，可为空
     * @param status        单据状态（0-草稿 1-已审核 2-已作废），可为空
     * @return 分页结果，包含总记录数和当前页销售订单列表
     */
    @PreAuthorize("@ps.hasPermission('sales:order:list')")
    @GetMapping("/page")
    public R<PageResult<SalesOrder>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long salespersonId,
            @RequestParam(required = false) String status) {
        return salesOrderService.page(pageNum, pageSize, orderNo, customerId, salespersonId, status);
    }

    /**
     * 查询销售订单详情（含明细列表）
     *
     * @param id 销售订单ID
     * @return 销售订单主表对象（含明细列表）
     */
    @PreAuthorize("@ps.hasPermission('sales:order:list')")
    @GetMapping("/{id}")
    public R<SalesOrder> detail(@PathVariable Long id) {
        return salesOrderService.detail(id);
    }

    /**
     * 新增销售订单（含明细）
     * 自动生成销售订单号，设置初始状态为草稿，自动计算明细金额、订单总金额及提成金额
     *
     * @param dto 销售订单请求DTO，包含order（销售订单主表）和items（明细列表）
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('sales:order:add')")
    @PostMapping
    public R<Void> add(@RequestBody SalesOrderDTO dto) {
        return salesOrderService.add(dto.getOrder(), dto.getItems());
    }

    /**
     * 审核销售订单
     * 更新单据状态为已审核，记录审核人和审核时间
     *
     * @param id      销售订单ID
     * @param auditBy 审核人ID
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('sales:order:audit')")
    @PutMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id, @RequestParam Long auditBy) {
        return salesOrderService.audit(id, auditBy);
    }

    /**
     * 作废销售订单
     * 更新单据状态为已作废
     *
     * @param id 销售订单ID
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('sales:order:void')")
    @PutMapping("/void/{id}")
    public R<Void> voidOrder(@PathVariable Long id) {
        return salesOrderService.voidOrder(id);
    }
}
