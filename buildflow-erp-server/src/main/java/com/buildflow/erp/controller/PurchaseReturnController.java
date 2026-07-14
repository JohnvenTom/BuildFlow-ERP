package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.dto.PurchaseReturnDTO;
import com.buildflow.erp.entity.PurchaseReturn;
import com.buildflow.erp.service.PurchaseReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 采购退货单管理控制器
 * 提供退货单的分页查询、新增（含明细）、审核、作废等接口
 */
@RestController
@RequestMapping("/purchase/return")
public class PurchaseReturnController {

    @Autowired
    private PurchaseReturnService purchaseReturnService;

    /**
     * 分页查询退货单列表
     * 支持按退货单号模糊搜索、供应商过滤、单据状态过滤
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param returnNo    退货单号（模糊查询），可为空
     * @param supplierId  供应商ID，可为空
     * @param status      单据状态（0-草稿 1-已审核 2-已作废），可为空
     * @return 分页结果，包含总记录数和当前页退货单列表
     */
    @GetMapping("/page")
    public R<PageResult<PurchaseReturn>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String returnNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status) {
        return purchaseReturnService.page(pageNum, pageSize, returnNo, supplierId, status);
    }

    /**
     * 新增退货单（含明细）
     * 自动生成退货单号，设置初始状态为草稿
     *
     * @param dto 退货单请求DTO，包含returnOrder（退货单主表）和items（明细列表）
     * @return 操作结果
     */
    @PostMapping
    public R<Void> add(@RequestBody PurchaseReturnDTO dto) {
        return purchaseReturnService.add(dto.getReturnOrder(), dto.getItems());
    }

    /**
     * 审核退货单
     * 事务操作：逐条明细扣减库存，更新单据状态为已审核，减少供应商应付台账
     *
     * @param id      退货单ID
     * @param auditBy 审核人ID
     * @return 操作结果
     */
    @PutMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id, @RequestParam Long auditBy) {
        return purchaseReturnService.audit(id, auditBy);
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
        return purchaseReturnService.voidOrder(id);
    }
}
