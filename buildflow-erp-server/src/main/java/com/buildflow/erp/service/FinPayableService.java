package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.FinPayable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 供应商应付台账服务接口
 * 定义应付账款管理的核心业务方法，包括分页查询应付台账、自动生成应付记录、按供应商汇总应付数据等操作。
 */
public interface FinPayableService {

    /**
     * 按供应商分页查询应付台账
     * 支持按供应商ID和状态过滤，按创建时间倒序排列
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param supplierId  供应商ID，可为null表示不过滤
     * @param status      状态（0-正常 1-逾期 2-已结清），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页应付台账列表
     */
    R<PageResult<FinPayable>> page(Integer pageNum, Integer pageSize, Long supplierId, String status);

    /**
     * 入库审核后自动生成应付记录
     * 根据供应商ID、来源单据类型和ID、应付金额、到期日创建应付台账，
     * 初始已付款金额为0，剩余金额等于应付金额，状态为正常。
     *
     * @param supplierId 供应商ID
     * @param sourceType 来源单据类型（如：STOCK_IN-入库单）
     * @param sourceId   来源单据ID
     * @param amount     应付金额
     * @param dueDate    到期日
     */
    void addPayable(Long supplierId, String sourceType, Long sourceId, BigDecimal amount, LocalDate dueDate);

    /**
     * 按供应商汇总应付总金额、已付款、剩余
     * 汇总所有未删除供应商的应付数据
     *
     * @return 汇总列表，每条包含supplierId、supplierName、totalAmount、paidAmount、remainAmount
     */
    R<List<Map<String, Object>>> summaryBySupplier();
}
