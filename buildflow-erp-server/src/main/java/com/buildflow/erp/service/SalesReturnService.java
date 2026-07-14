package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SalesReturn;
import com.buildflow.erp.entity.SalesReturnItem;

import java.util.List;

/**
 * 销售退货单服务接口
 * 定义销售退货管理的核心业务方法，包括分页查询、新增退货单（含明细）、审核退货单、作废退货单等操作。
 * 审核退货单时通过事务返还库存、减少客户欠款。
 */
public interface SalesReturnService {

    /**
     * 分页查询退货单列表
     * 支持按退货单号模糊搜索、客户、单据状态过滤
     *
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param returnNo   退货单号（模糊查询），可为null
     * @param customerId 客户ID，可为null表示不过滤
     * @param status     单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页退货单列表
     */
    R<PageResult<SalesReturn>> page(Integer pageNum, Integer pageSize, String returnNo, Long customerId, String status);

    /**
     * 新增销售退货单（含明细）
     * 自动生成退货单号（SR+日期+流水号），设置初始状态为草稿，
     * 自动计算退货总金额，插入主表和明细表
     *
     * @param returnOrder 退货单主表对象，需包含customerId、deliveryId等字段
     * @param items       退货单明细列表，每条需包含productId、quantity、unitPrice
     * @return 操作结果
     */
    R<Void> add(SalesReturn returnOrder, List<SalesReturnItem> items);

    /**
     * 审核退货单
     * 事务操作：校验单据状态为草稿，查询关联出库单获取仓库ID，
     * 逐条明细调用increaseStock返还库存，减少客户应收台账（预留），减少客户当前欠款，更新状态为已审核。
     * 注意：客户应收台账减少功能待FinReceivableService完成后集成。
     *
     * @param id      退货单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     */
    R<Void> audit(Long id, Long auditBy);

    /**
     * 作废退货单
     * 仅草稿状态的退货单可作废
     *
     * @param id 退货单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    R<Void> voidOrder(Long id);
}
