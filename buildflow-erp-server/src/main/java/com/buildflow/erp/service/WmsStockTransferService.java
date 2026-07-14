package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsStockTransfer;
import com.buildflow.erp.entity.WmsStockTransferItem;

import java.util.List;

/**
 * 调拨单服务接口
 * 定义仓库间库存调拨的核心业务方法，包括分页查询、新增调拨单（含明细）、审核调拨单、作废调拨单等操作。
 */
public interface WmsStockTransferService {

    /**
     * 分页查询调拨单列表
     * 支持按调拨单号模糊搜索、单据状态过滤
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param transferNo  调拨单号（模糊查询），可为null
     * @param status      单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页调拨单列表
     */
    R<PageResult<WmsStockTransfer>> page(Integer pageNum, Integer pageSize, String transferNo, String status);

    /**
     * 新增调拨单（含明细）
     * 自动生成调拨单号，设置初始状态为草稿，插入主表和明细表
     *
     * @param transfer 调拨单主表对象，需包含fromWarehouseId、toWarehouseId等字段
     * @param items    调拨单明细列表，每条需包含productId和quantity
     * @return 操作结果
     */
    R<Void> add(WmsStockTransfer transfer, List<WmsStockTransferItem> items);

    /**
     * 审核调拨单
     * 事务操作：校验单据状态为草稿，更新状态为已审核，逐条明细扣减调出仓库存并增加调入仓库存
     *
     * @param id      调拨单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     * @throws com.buildflow.erp.common.exception.BusinessException 调出仓库存不足时抛出
     */
    R<Void> audit(Long id, Long auditBy);

    /**
     * 作废调拨单
     * 仅草稿状态或已审核状态的调拨单可作废，已审核单据作废不回退库存
     *
     * @param id 调拨单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    R<Void> voidOrder(Long id);
}
