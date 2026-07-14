package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsStockTransfer;
import com.buildflow.erp.entity.WmsStockTransferItem;
import com.buildflow.erp.service.WmsStockTransferService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 调拨单管理控制器
 * 提供调拨单的分页查询、新增（含明细）、审核、作废等接口
 */
@RestController
@RequestMapping("/warehouse/transfer")
public class WmsStockTransferController {

    @Autowired
    private WmsStockTransferService wmsStockTransferService;

    /**
     * 分页查询调拨单列表
     * 支持按调拨单号模糊搜索、单据状态过滤
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param transferNo  调拨单号（模糊查询），可为空
     * @param status      单据状态（0-草稿 1-已审核 2-已作废），可为空
     * @return 分页结果，包含总记录数和当前页调拨单列表
     */
    @GetMapping("/page")
    public R<PageResult<WmsStockTransfer>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String transferNo,
            @RequestParam(required = false) String status) {
        return wmsStockTransferService.page(pageNum, pageSize, transferNo, status);
    }

    /**
     * 新增调拨单（含明细）
     * 自动生成调拨单号，设置初始状态为草稿
     *
     * @param request 调拨单请求体，包含transfer（调拨单主表）和items（明细列表）
     * @return 操作结果
     */
    @PostMapping
    public R<Void> add(@RequestBody TransferRequest request) {
        return wmsStockTransferService.add(request.getTransfer(), request.getItems());
    }

    /**
     * 审核调拨单
     * 事务操作：扣减调出仓库存并增加调入仓库存，更新单据状态为已审核
     *
     * @param id      调拨单ID
     * @param auditBy 审核人ID
     * @return 操作结果
     */
    @PutMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id, @RequestParam Long auditBy) {
        return wmsStockTransferService.audit(id, auditBy);
    }

    /**
     * 作废调拨单
     * 更新单据状态为已作废
     *
     * @param id 调拨单ID
     * @return 操作结果
     */
    @PutMapping("/void/{id}")
    public R<Void> voidOrder(@PathVariable Long id) {
        return wmsStockTransferService.voidOrder(id);
    }

    /**
     * 调拨单新增请求体
     * 封装调拨单主表对象和明细列表，用于新增接口的请求参数接收
     */
    @Data
    public static class TransferRequest {
        /** 调拨单主表对象 */
        private WmsStockTransfer transfer;

        /** 调拨单明细列表 */
        private List<WmsStockTransferItem> items;
    }
}
