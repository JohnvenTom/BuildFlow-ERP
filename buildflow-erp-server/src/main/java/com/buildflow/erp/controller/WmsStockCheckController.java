package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsStockCheck;
import com.buildflow.erp.entity.WmsStockCheckItem;
import com.buildflow.erp.service.WmsStockCheckService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 盘点单管理控制器
 * 提供盘点单的分页查询、新增（含明细）、审核、作废等接口
 */
@RestController
@RequestMapping("/warehouse/check")
public class WmsStockCheckController {

    @Autowired
    private WmsStockCheckService wmsStockCheckService;

    /**
     * 分页查询盘点单列表
     * 支持按盘点单号模糊搜索、单据状态过滤
     *
     * @param pageNum   当前页码，从1开始
     * @param pageSize  每页记录数
     * @param checkNo   盘点单号（模糊查询），可为空
     * @param status    单据状态（0-草稿 1-已审核 2-已作废），可为空
     * @return 分页结果，包含总记录数和当前页盘点单列表
     */
    @PreAuthorize("@ps.hasPermission('wms:check:list')")
    @GetMapping("/page")
    public R<PageResult<WmsStockCheck>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String checkNo,
            @RequestParam(required = false) String status) {
        return wmsStockCheckService.page(pageNum, pageSize, checkNo, status);
    }

    /**
     * 新增盘点单（含明细）
     * 自动生成盘点单号，设置初始状态为草稿，自动计算差异数量
     *
     * @param request 盘点单请求体，包含check（盘点单主表）和items（明细列表）
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('wms:check:add')")
    @PostMapping
    public R<Void> add(@RequestBody CheckRequest request) {
        return wmsStockCheckService.add(request.getCheck(), request.getItems());
    }

    /**
     * 审核盘点单
     * 事务操作：根据盘盈盘亏修正库存，更新单据状态为已审核
     *
     * @param id      盘点单ID
     * @param auditBy 审核人ID
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('wms:check:audit')")
    @PutMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id, @RequestParam Long auditBy) {
        return wmsStockCheckService.audit(id, auditBy);
    }

    /**
     * 作废盘点单
     * 更新单据状态为已作废
     *
     * @param id 盘点单ID
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('wms:check:void')")
    @PutMapping("/void/{id}")
    public R<Void> voidOrder(@PathVariable Long id) {
        return wmsStockCheckService.voidOrder(id);
    }

    /**
     * 盘点单新增请求体
     * 封装盘点单主表对象和明细列表，用于新增接口的请求参数接收
     */
    @Data
    public static class CheckRequest {
        /** 盘点单主表对象 */
        private WmsStockCheck check;

        /** 盘点单明细列表 */
        private List<WmsStockCheckItem> items;
    }
}
