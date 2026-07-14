package com.buildflow.erp.service;

import com.buildflow.erp.common.result.R;

import java.util.List;
import java.util.Map;

/**
 * 仓储预警服务接口
 * 定义库存预警的核心业务方法，包括低库存预警和呆滞库存预警。
 */
public interface WmsAlertService {

    /**
     * 低库存预警
     * 查询所有商品在各仓库的库存总量低于最低库存阈值的记录
     *
     * @return 预警列表，每条记录包含商品ID、商品名称、仓库ID、仓库名称、当前库存、最低库存等字段
     */
    R<List<Map<String, Object>>> lowStockAlert();

    /**
     * 呆滞库存预警
     * 查询库存更新时间超过90天无出库变动的库存记录
     *
     * @return 预警列表，每条记录包含商品ID、商品名称、仓库ID、仓库名称、批次号、库存数量、最后更新时间等字段
     */
    R<List<Map<String, Object>>> dormantStockAlert();
}
