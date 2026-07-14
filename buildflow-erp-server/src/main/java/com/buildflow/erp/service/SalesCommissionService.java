package com.buildflow.erp.service;

import com.buildflow.erp.common.result.R;

import java.util.List;
import java.util.Map;

/**
 * 销售提成服务接口
 * 定义销售提成统计相关业务方法，提供按时间段汇总业务员提成金额的报表查询。
 */
public interface SalesCommissionService {

    /**
     * 销售提成汇总报表
     * 按时间段汇总每个业务员全部已审核销售订单的提成金额，关联sys_user获取业务员姓名
     *
     * @param startTime 查询开始时间（格式：yyyy-MM-dd HH:mm:ss），可为null表示不限制起始时间
     * @param endTime   查询结束时间（格式：yyyy-MM-dd HH:mm:ss），可为null表示不限制截止时间
     * @return 业务员提成汇总列表，每个元素为Map，包含salespersonId、salespersonName、orderCount、totalCommission等字段
     */
    R<List<Map<String, Object>>> commissionReport(String startTime, String endTime);
}
