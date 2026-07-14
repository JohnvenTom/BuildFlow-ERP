package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.FinPaymentReceive;
import com.buildflow.erp.entity.FinPaymentReceiveItem;

import java.util.List;

/**
 * 客户回款单服务接口
 * 定义回款单管理的核心业务方法，包括分页查询、新增回款单（含核销明细）、审核回款单、作废回款单等操作。
 */
public interface FinPaymentReceiveService {

    /**
     * 分页查询回款单列表
     * 支持按回款单号模糊搜索、客户ID、状态过滤，按创建时间倒序排列
     *
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param receiveNo  回款单号（模糊查询），可为null
     * @param customerId 客户ID，可为null表示不过滤
     * @param status     单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页回款单列表
     */
    R<PageResult<FinPaymentReceive>> page(Integer pageNum, Integer pageSize, String receiveNo, Long customerId, String status);

    /**
     * 新增回款单及核销明细
     * 自动生成回款单号（RC+日期+流水号），设置初始状态为草稿，
     * 校验核销明细总金额不超过回款总金额，插入主表和明细表
     *
     * @param receive 回款单主表对象，需包含customerId、amount、payMethod等字段
     * @param items   核销明细列表，每条需包含receivableId、amount
     * @return 操作结果，核销明细总金额超过回款金额时返回R.fail()
     */
    R<Void> add(FinPaymentReceive receive, List<FinPaymentReceiveItem> items);

    /**
     * 审核回款单
     * 事务操作：校验单据状态为草稿，逐条明细核销应收台账（增加paidAmount、减少remainAmount），
     * 若剩余金额为0则更新状态为已结清，减少客户当前欠款，更新回款单状态为已审核。
     *
     * @param id      回款单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     */
    R<Void> audit(Long id, Long auditBy);

    /**
     * 作废回款单
     * 仅草稿状态的回款单可作废，已审核的回款单不可作废（需通过红冲方式处理）
     *
     * @param id 回款单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    R<Void> voidOrder(Long id);
}
