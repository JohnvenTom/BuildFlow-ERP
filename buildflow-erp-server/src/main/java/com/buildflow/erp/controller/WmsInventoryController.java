package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsInventory;
import com.buildflow.erp.service.WmsInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 库存明细管理控制器
 * 提供库存明细分页查询接口，支持按仓库、商品、批次号过滤
 */
@RestController
@RequestMapping("/warehouse/inventory")
public class WmsInventoryController {

    @Autowired
    private WmsInventoryService wmsInventoryService;

    /**
     * 分页查询库存明细
     * 支持按仓库ID、商品ID、批次号过滤
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param warehouseId 仓库ID，可为空
     * @param productId   商品ID，可为空
     * @param batchNo     批次号（模糊查询），可为空
     * @return 分页结果，包含总记录数和当前页库存明细列表
     */
    @GetMapping("/page")
    public R<PageResult<WmsInventory>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String batchNo) {
        return wmsInventoryService.page(pageNum, pageSize, warehouseId, productId, batchNo);
    }
}
