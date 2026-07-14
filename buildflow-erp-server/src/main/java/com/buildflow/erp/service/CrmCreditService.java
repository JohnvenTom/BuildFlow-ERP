package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;

import java.util.Map;

/**
 * 信用与逾期查询服务接口
 * 定义客户信用额度、应收总额、逾期金额、逾期天数等查询方法。
 */
public interface CrmCreditService {

    /**
     * 分页查询客户信用与逾期信息
     * 查询所有客户的当前应收总额、信用剩余额度、逾期金额、逾期天数
     *
     * @param pageNum       当前页码，从1开始
     * @param pageSize      每页记录数
     * @param customerName  客户名称（模糊查询），可为null
     * @param debtStatus    欠款状态（如：overdue-逾期、normal-正常），可为null表示不过滤
     * @return 分页结果，每条记录为客户信用信息Map，包含customerId、customerName、
     *         creditLimit、currentDebt、remainCredit、overdueAmount、overdueDays等字段
     */
    R<PageResult<Map<String, Object>>> creditPage(Integer pageNum, Integer pageSize,
                                                   String customerName, String debtStatus);
}
