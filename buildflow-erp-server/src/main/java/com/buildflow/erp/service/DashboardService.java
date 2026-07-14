package com.buildflow.erp.service;

import com.buildflow.erp.common.result.R;
import com.buildflow.erp.dto.DashboardData;

/**
 * 数据大屏服务接口
 * 提供经营数据大屏的聚合统计功能，一次调用返回所有大屏所需的指标数据。
 */
public interface DashboardService {

    /**
     * 聚合统计所有大屏数据
     * 包括今日销售额、本月总销量、总应收欠款、呆滞库存、客户类型销量分布、
     * 商品热销排行、逾期欠款客户、近30日销售趋势、各仓库库存总量等。
     *
     * @return 大屏聚合数据
     */
    R<DashboardData> getDashboardData();
}
