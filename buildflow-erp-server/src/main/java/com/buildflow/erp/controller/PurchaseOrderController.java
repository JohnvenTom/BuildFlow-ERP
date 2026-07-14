package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.dto.PurchaseOrderDTO;
import com.buildflow.erp.entity.PurchaseOrder;
import com.buildflow.erp.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 采购订单管理控制器
 * 提供采购订单的分页查询、新增（含明细）、审核、作废等接口
 */
@RestController
@RequestMapping("/purchase/order")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    /**
     * 分页查询采购订单列表
     * 支持按采购订单号模糊搜索、供应商过滤、单据状态过滤
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param orderNo     采购订单号（模糊查询），可为空
     * @param supplierId  供应商ID，可为空
     * @param status      单据状态（0-草稿 1-已审核 2-已作废），可为空
     * @return 分页结果，包含总记录数和当前页采购订单列表
     */
    @PreAuthorize("@ps.hasPermission('purchase:order:list')")
    @GetMapping("/page")
    public R<PageResult<PurchaseOrder>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status) {
        return purchaseOrderService.page(pageNum, pageSize, orderNo, supplierId, status);
    }

    /**
     * 新增采购订单（含明细）
     * 自动生成采购订单号，设置初始状态为草稿，自动计算明细金额和订单总金额
     *
     * @param dto 采购订单请求DTO，包含order（采购订单主表）和items（明细列表）
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('purchase:order:add')")
    @PostMapping
    public R<Void> add(@RequestBody PurchaseOrderDTO dto) {
        return purchaseOrderService.add(dto.getOrder(), dto.getItems());
    }

    /**
     * 审核采购订单
     * 更新单据状态为已审核，记录审核人和审核时间
     *
     * @param id      采购订单ID
     * @param auditBy 审核人ID
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('purchase:order:audit')")
    @PutMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id, @RequestParam Long auditBy) {
        return purchaseOrderService.audit(id, auditBy);
    }

    /**
     * 作废采购订单
     * 更新单据状态为已作废
     *
     * @param id 采购订单ID
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('purchase:order:void')")
    @PutMapping("/void/{id}")
    public R<Void> voidOrder(@PathVariable Long id) {
        return purchaseOrderService.voidOrder(id);
    }
}
