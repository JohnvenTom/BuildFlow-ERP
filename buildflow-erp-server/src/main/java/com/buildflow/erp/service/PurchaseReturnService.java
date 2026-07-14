package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.PurchaseReturn;
import com.buildflow.erp.entity.PurchaseReturnItem;

import java.util.List;

/**
 * 采购退货单服务接口
 * 定义采购退货管理的核心业务方法，包括分页查询、新增退货单（含明细）、审核退货单、作废退货单等操作。
 * 审核退货单时通过事务调用WmsInventoryService.decreaseStock扣减库存并减少供应商应付台账。
 */
public interface PurchaseReturnService {

    /**
     * 分页查询退货单列表
     * 支持按退货单号模糊搜索、供应商过滤、单据状态过滤
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param returnNo    退货单号（模糊查询），可为null
     * @param supplierId  供应商ID，可为null表示不过滤
     * @param status      单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页退货单列表
     */
    R<PageResult<PurchaseReturn>> page(Integer pageNum, Integer pageSize, String returnNo, Long supplierId, String status);

    /**
     * 新增退货单（含明细）
     * 自动生成退货单号（PR+日期+流水号），设置初始状态为草稿，插入主表和明细表
     *
     * @param returnOrder 退货单主表对象，需包含supplierId、stockInId等字段
     * @param items       退货单明细列表，每条需包含productId、quantity、unitPrice
     * @return 操作结果
     */
    R<Void> add(PurchaseReturn returnOrder, List<PurchaseReturnItem> items);

    /**
     * 审核退货单
     * 事务操作：校验单据状态为草稿，查询关联入库单获取仓库ID，逐条明细调用decreaseStock扣减库存，
     * 更新状态为已审核，减少供应商应付台账。
     * 注意：应付台账减少功能待FinPayableService完成后集成。
     *
     * @param id      退货单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     * @throws com.buildflow.erp.common.exception.BusinessException 库存不足时抛出，事务回滚
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
