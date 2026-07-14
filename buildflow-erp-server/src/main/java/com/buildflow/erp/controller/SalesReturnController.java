package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.dto.SalesReturnDTO;
import com.buildflow.erp.entity.SalesReturn;
import com.buildflow.erp.service.SalesReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 销售退货单管理控制器
 * 提供销售退货单的分页查询、新增（含明细）、审核、作废等接口
 */
@RestController
@RequestMapping("/sales/return")
public class SalesReturnController {

    @Autowired
    private SalesReturnService salesReturnService;

    /**
     * 分页查询退货单列表
     * 支持按退货单号模糊搜索、客户、单据状态过滤
     *
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param returnNo   退货单号（模糊查询），可为空
     * @param customerId 客户ID，可为空
     * @param status     单据状态（0-草稿 1-已审核 2-已作废），可为空
     * @return 分页结果，包含总记录数和当前页退货单列表
     */
    @GetMapping("/page")
    public R<PageResult<SalesReturn>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String returnNo,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status) {
        return salesReturnService.page(pageNum, pageSize, returnNo, customerId, status);
    }

    /**
     * 新增销售退货单（含明细）
     * 自动生成退货单号，设置初始状态为草稿，自动计算退货总金额
     *
     * @param dto 退货单请求DTO，包含returnOrder（退货单主表）和items（明细列表）
     * @return 操作结果
     */
    @PostMapping
    public R<Void> add(@RequestBody SalesReturnDTO dto) {
        return salesReturnService.add(dto.getReturnOrder(), dto.getItems());
    }

    /**
     * 审核退货单
     * 事务操作：返还库存、减少客户应收台账（预留）、减少客户欠款
     *
     * @param id      退货单ID
     * @param auditBy 审核人ID
     * @return 操作结果
     */
    @PutMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id, @RequestParam Long auditBy) {
        return salesReturnService.audit(id, auditBy);
    }

    /**
     * 作废退货单
     * 更新单据状态为已作废
     *
     * @param id 退货单ID
     * @return 操作结果
     */
    @PutMapping("/void/{id}")
    public R<Void> voidOrder(@PathVariable Long id) {
        return salesReturnService.voidOrder(id);
    }
}
