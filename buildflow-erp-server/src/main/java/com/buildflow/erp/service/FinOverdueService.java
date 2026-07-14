package com.buildflow.erp.service;

import com.buildflow.erp.common.result.R;

import java.util.List;
import java.util.Map;

/**
 * 逾期应收服务接口
 * 定义逾期应收账款查询的核心业务方法，包括查询所有逾期应收明细、按客户汇总逾期总金额等操作。
 */
public interface FinOverdueService {

    /**
     * 查询所有逾期应收明细
     * 查询所有未结清且到期日早于今天的应收台账，关联客户名称，计算逾期天数
     *
     * @return 逾期明细列表，每条包含id、customerId、customerName、remainAmount、dueDate、overdueDays
     */
    R<List<Map<String, Object>>> overdueList();

    /**
     * 按客户汇总逾期总金额
     * 按客户ID分组汇总所有逾期应收的剩余金额
     *
     * @return 汇总列表，每条包含customerId、customerName、overdueAmount
     */
    R<List<Map<String, Object>>> overdueCustomerSummary();
}
