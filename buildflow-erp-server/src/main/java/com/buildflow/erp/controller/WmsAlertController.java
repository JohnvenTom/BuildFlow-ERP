package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.R;
import com.buildflow.erp.service.WmsAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 仓储预警控制器
 * 提供低库存预警和呆滞库存预警查询接口
 */
@RestController
@RequestMapping("/warehouse/alert")
public class WmsAlertController {

    @Autowired
    private WmsAlertService wmsAlertService;

    /**
     * 低库存预警
     * 查询所有商品的库存总量低于最低库存阈值的记录
     *
     * @return 预警列表，每条记录包含商品ID、商品名称、分类、当前总库存、最低库存等字段
     */
    @PreAuthorize("@ps.hasPermission('wms:alert:list')")
    @GetMapping("/lowStock")
    public R<List<Map<String, Object>>> lowStockAlert() {
        return wmsAlertService.lowStockAlert();
    }

    /**
     * 呆滞库存预警
     * 查询库存更新时间超过90天无出库变动的库存记录
     *
     * @return 预警列表，每条记录包含商品ID、商品名称、仓库ID、仓库名称、批次号、库存数量、最后更新时间等字段
     */
    @PreAuthorize("@ps.hasPermission('wms:alert:list')")
    @GetMapping("/dormant")
    public R<List<Map<String, Object>>> dormantStockAlert() {
        return wmsAlertService.dormantStockAlert();
    }
}
