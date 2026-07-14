package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.dto.PurchaseStockInDTO;
import com.buildflow.erp.entity.PurchaseStockIn;
import com.buildflow.erp.service.PurchaseStockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 采购入库单管理控制器
 * 提供入库单的分页查询、新增（含明细）、审核、作废等接口
 */
@RestController
@RequestMapping("/purchase/stock-in")
public class PurchaseStockInController {

    @Autowired
    private PurchaseStockInService purchaseStockInService;

    /**
     * 分页查询入库单列表
     * 支持按入库单号模糊搜索、供应商过滤、单据状态过滤
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param stockInNo   入库单号（模糊查询），可为空
     * @param supplierId  供应商ID，可为空
     * @param status      单据状态（0-草稿 1-已审核 2-已作废），可为空
     * @return 分页结果，包含总记录数和当前页入库单列表
     */
    @GetMapping("/page")
    public R<PageResult<PurchaseStockIn>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String stockInNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status) {
        return purchaseStockInService.page(pageNum, pageSize, stockInNo, supplierId, status);
    }

    /**
     * 新增入库单（含明细）
     * 自动生成入库单号，设置初始状态为草稿
     *
     * @param dto 入库单请求DTO，包含stockIn（入库单主表）和items（明细列表）
     * @return 操作结果
     */
    @PostMapping
    public R<Void> add(@RequestBody PurchaseStockInDTO dto) {
        return purchaseStockInService.add(dto.getStockIn(), dto.getItems());
    }

    /**
     * 审核入库单
     * 事务操作：逐条明细增加库存，更新单据状态为已审核，生成供应商应付台账
     *
     * @param id      入库单ID
     * @param auditBy 审核人ID
     * @return 操作结果
     */
    @PutMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id, @RequestParam Long auditBy) {
        return purchaseStockInService.audit(id, auditBy);
    }

    /**
     * 作废入库单
     * 更新单据状态为已作废
     *
     * @param id 入库单ID
     * @return 操作结果
     */
    @PutMapping("/void/{id}")
    public R<Void> voidOrder(@PathVariable Long id) {
        return purchaseStockInService.voidOrder(id);
    }
}
