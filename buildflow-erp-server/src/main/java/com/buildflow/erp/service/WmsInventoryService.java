package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsInventory;

/**
 * 库存明细服务接口
 * 定义库存管理的核心业务方法，包括分页查询、增加库存、扣减库存等操作。
 * 增加和扣减库存方法供调拨单、盘点单审核时调用，非直接对外暴露。
 */
public interface WmsInventoryService {

    /**
     * 分页查询库存明细
     * 支持按仓库、商品、批次号过滤
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param warehouseId 仓库ID，可为null表示不过滤
     * @param productId   商品ID，可为null表示不过滤
     * @param batchNo     批次号（模糊查询），可为null
     * @return 分页结果，包含总记录数和当前页库存明细列表
     */
    R<PageResult<WmsInventory>> page(Integer pageNum, Integer pageSize, Long warehouseId, Long productId, String batchNo);

    /**
     * 增加库存
     * 按 商品+仓库+批次号+色号 维度匹配库存记录，存在则累加数量，不存在则新增记录
     *
     * @param productId   商品ID
     * @param warehouseId 仓库ID
     * @param batchNo     批次号
     * @param colorNo     色号
     * @param quantity    增加数量，必须大于0
     */
    void increaseStock(Long productId, Long warehouseId, String batchNo, String colorNo, Integer quantity);

    /**
     * 扣减库存
     * 按 商品+仓库+批次号+色号 维度匹配库存记录，存在且数量充足则扣减，不足则抛出BusinessException
     *
     * @param productId   商品ID
     * @param warehouseId 仓库ID
     * @param batchNo     批次号
     * @param colorNo     色号
     * @param quantity    扣减数量，必须大于0
     * @throws com.buildflow.erp.common.exception.BusinessException 库存记录不存在或库存不足时抛出
     */
    void decreaseStock(Long productId, Long warehouseId, String batchNo, String colorNo, Integer quantity);
}
