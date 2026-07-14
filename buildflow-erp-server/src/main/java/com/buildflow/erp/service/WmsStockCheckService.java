package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsStockCheck;
import com.buildflow.erp.entity.WmsStockCheckItem;

import java.util.List;

/**
 * 盘点单服务接口
 * 定义仓库库存盘点的核心业务方法，包括分页查询、新增盘点单（含明细）、审核盘点单、作废盘点单等操作。
 */
public interface WmsStockCheckService {

    /**
     * 分页查询盘点单列表
     * 支持按盘点单号模糊搜索、单据状态过滤
     *
     * @param pageNum   当前页码，从1开始
     * @param pageSize  每页记录数
     * @param checkNo   盘点单号（模糊查询），可为null
     * @param status    单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页盘点单列表
     */
    R<PageResult<WmsStockCheck>> page(Integer pageNum, Integer pageSize, String checkNo, String status);

    /**
     * 新增盘点单（含明细）
     * 自动生成盘点单号，设置初始状态为草稿，自动计算差异数量（actualQty - systemQty），插入主表和明细表
     *
     * @param check 盘点单主表对象，需包含warehouseId等字段
     * @param items 盘点单明细列表，每条需包含productId、systemQty、actualQty
     * @return 操作结果
     */
    R<Void> add(WmsStockCheck check, List<WmsStockCheckItem> items);

    /**
     * 审核盘点单
     * 事务操作：校验单据状态为草稿，更新状态为已审核，根据差异修正库存（盘盈增加库存，盘亏扣减库存）
     *
     * @param id      盘点单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     * @throws com.buildflow.erp.common.exception.BusinessException 盘亏时库存不足抛出
     */
    R<Void> audit(Long id, Long auditBy);

    /**
     * 作废盘点单
     * 仅草稿状态的盘点单可作废
     *
     * @param id 盘点单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    R<Void> voidOrder(Long id);
}
