package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.R;
import com.buildflow.erp.dto.DashboardData;
import com.buildflow.erp.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据大屏控制器
 * 提供经营数据大屏的聚合查询接口，一次请求返回所有大屏统计指标。
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 聚合返回所有大屏数据
     * 包括核心指标卡片、图表数据、表格数据等，供前端大屏页面使用。
     *
     * @return 大屏聚合数据，包含今日销售额、本月总销量、总应收欠款、呆滞库存数量、
     *         客户类型销量分布、商品热销排行、逾期欠款客户、近30日销售趋势、各仓库库存总量
     */
    @GetMapping("/data")
    public R<DashboardData> getDashboardData() {
        return dashboardService.getDashboardData();
    }
}
