package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.PurchaseStockIn;
import com.buildflow.erp.entity.PurchaseStockInItem;

import java.util.List;

/**
 * 采购入库单服务接口
 * 定义采购入库管理的核心业务方法，包括分页查询、新增入库单（含明细）、审核入库单、作废入库单等操作。
 * 审核入库单时通过事务调用WmsInventoryService.increaseStock增加库存并生成供应商应付台账。
 */
public interface PurchaseStockInService {

    /**
     * 分页查询入库单列表
     * 支持按入库单号模糊搜索、供应商过滤、单据状态过滤
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param stockInNo   入库单号（模糊查询），可为null
     * @param supplierId  供应商ID，可为null表示不过滤
     * @param status      单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页入库单列表
     */
    R<PageResult<PurchaseStockIn>> page(Integer pageNum, Integer pageSize, String stockInNo, Long supplierId, String status);

    /**
     * 新增入库单（含明细）
     * 自动生成入库单号（SI+日期+流水号），设置初始状态为草稿，插入主表和明细表
     *
     * @param stockIn 入库单主表对象，需包含orderId、supplierId、warehouseId等字段
     * @param items   入库单明细列表，每条需包含productId、batchNo、quantity、unitPrice
     * @return 操作结果
     */
    R<Void> add(PurchaseStockIn stockIn, List<PurchaseStockInItem> items);

    /**
     * 审核入库单
     * 事务操作：校验单据状态为草稿，逐条明细调用increaseStock增加库存，更新状态为已审核，生成供应商应付台账。
     * 注意：应付台账生成功能待FinPayableService完成后集成。
     *
     * @param id      入库单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     */
    R<Void> audit(Long id, Long auditBy);

    /**
     * 作废入库单
     * 仅草稿状态的入库单可作废
     *
     * @param id 入库单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    R<Void> voidOrder(Long id);
}
