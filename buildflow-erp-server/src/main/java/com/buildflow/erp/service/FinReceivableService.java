package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.FinReceivable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 客户应收台账服务接口
 * 定义应收账款管理的核心业务方法，包括分页查询应收台账、自动生成应收记录、按客户汇总应收数据等操作。
 */
public interface FinReceivableService {

    /**
     * 按客户分页查询应收台账
     * 支持按客户ID和状态过滤，按创建时间倒序排列
     *
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param customerId 客户ID，可为null表示不过滤
     * @param status     状态（0-正常 1-逾期 2-已结清），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页应收台账列表
     */
    R<PageResult<FinReceivable>> page(Integer pageNum, Integer pageSize, Long customerId, String status);

    /**
     * 出库审核后自动生成应收记录
     * 根据客户ID、来源单据类型和ID、应收金额、到期日创建应收台账，
     * 初始已回款金额为0，剩余金额等于应收金额，状态为正常。
     *
     * @param customerId 客户ID
     * @param sourceType 来源单据类型（如：DELIVERY-出库单）
     * @param sourceId   来源单据ID
     * @param amount     应收金额
     * @param dueDate    到期日
     */
    void addReceivable(Long customerId, String sourceType, Long sourceId, BigDecimal amount, LocalDate dueDate);

    /**
     * 按客户汇总应收总金额、已回款、剩余、逾期金额
     * 汇总所有未删除客户的应收数据，逾期金额按状态为逾期且剩余大于0的记录统计
     *
     * @return 汇总列表，每条包含customerId、customerName、totalAmount、paidAmount、remainAmount、overdueAmount
     */
    R<List<Map<String, Object>>> summaryByCustomer();
}
