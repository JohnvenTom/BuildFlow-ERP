package com.buildflow.erp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 数据大屏聚合DTO
 * 封装经营数据大屏所有统计指标，包括核心指标卡片、图表数据、表格数据等。
 * 用于前端大屏页面的单次接口聚合返回，减少多接口调用。
 */
@Data
public class DashboardData {

    /** 今日销售额 */
    private BigDecimal todaySales;

    /** 本月总销量 */
    private BigDecimal monthSales;

    /** 总应收欠款（所有未结清的剩余应收金额合计） */
    private BigDecimal totalReceivable;

    /** 呆滞库存数量（90天无出库记录的商品数量） */
    private Integer dormantStockCount;

    /** 客户类型销量分布 [{type: 客户类型, amount: 销售金额}] */
    private List<Map<String, Object>> customerTypeSales;

    /** 商品热销排行TOP10 [{name: 商品名称, amount: 销售金额}] */
    private List<Map<String, Object>> hotProducts;

    /** 逾期欠款客户榜单 [{name: 客户名称, amount: 逾期金额, days: 逾期天数}] */
    private List<Map<String, Object>> overdueCustomers;

    /** 近30日销售额趋势 [{date: 日期, amount: 销售金额}] */
    private List<Map<String, Object>> salesTrend;

    /** 各仓库库存总量 [{name: 仓库名称, quantity: 库存数量}] */
    private List<Map<String, Object>> warehouseStock;
}
