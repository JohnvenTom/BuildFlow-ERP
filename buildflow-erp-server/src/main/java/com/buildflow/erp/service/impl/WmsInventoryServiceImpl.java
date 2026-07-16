package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.exception.BusinessException;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsInventory;
import com.buildflow.erp.entity.WmsProduct;
import com.buildflow.erp.entity.WmsWarehouse;
import com.buildflow.erp.mapper.WmsInventoryMapper;
import com.buildflow.erp.mapper.WmsProductMapper;
import com.buildflow.erp.mapper.WmsWarehouseMapper;
import com.buildflow.erp.service.WmsInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 库存明细服务实现类
 * 实现库存管理的核心业务逻辑，包括分页查询、增加库存、扣减库存等操作。
 * 增加和扣减库存方法供调拨单、盘点单审核时调用，保证库存数据一致性。
 */
@Service
public class WmsInventoryServiceImpl implements WmsInventoryService {

    @Autowired
    private WmsInventoryMapper wmsInventoryMapper;

    @Autowired
    private WmsProductMapper wmsProductMapper;

    @Autowired
    private WmsWarehouseMapper wmsWarehouseMapper;

    /**
     * 分页查询库存明细
     * 支持按仓库ID、商品ID、批次号过滤，按更新时间倒序排列
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param warehouseId 仓库ID，可为null表示不过滤
     * @param productId   商品ID，可为null表示不过滤
     * @param batchNo     批次号（模糊查询），可为null
     * @return 分页结果，包含总记录数和当前页库存明细列表
     */
    @Override
    public R<PageResult<WmsInventory>> page(Integer pageNum, Integer pageSize, Long warehouseId, Long productId, String batchNo) {
        Page<WmsInventory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WmsInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, WmsInventory::getWarehouseId, warehouseId)
                .eq(productId != null, WmsInventory::getProductId, productId)
                .like(StringUtils.hasText(batchNo), WmsInventory::getBatchNo, batchNo)
                .orderByDesc(WmsInventory::getUpdateTime);
        Page<WmsInventory> result = wmsInventoryMapper.selectPage(page, wrapper);
        List<WmsInventory> records = result.getRecords();
        // 批量查询商品名称和仓库名称并填充到结果中
        if (!records.isEmpty()) {
            Set<Long> productIds = records.stream()
                    .map(WmsInventory::getProductId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<Long> warehouseIds = records.stream()
                    .map(WmsInventory::getWarehouseId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!productIds.isEmpty()) {
                List<WmsProduct> products = wmsProductMapper.selectBatchIds(productIds);
                Map<Long, String> productMap = products.stream()
                        .collect(Collectors.toMap(WmsProduct::getId, WmsProduct::getName));
                records.forEach(i -> i.setProductName(productMap.get(i.getProductId())));
            }
            if (!warehouseIds.isEmpty()) {
                List<WmsWarehouse> warehouses = wmsWarehouseMapper.selectBatchIds(warehouseIds);
                Map<Long, String> warehouseMap = warehouses.stream()
                        .collect(Collectors.toMap(WmsWarehouse::getId, WmsWarehouse::getName));
                records.forEach(i -> i.setWarehouseName(warehouseMap.get(i.getWarehouseId())));
            }
        }
        return R.ok(new PageResult<>(result.getTotal(), records));
    }

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
    @Override
    public void increaseStock(Long productId, Long warehouseId, String batchNo, String colorNo, Integer quantity) {
        // 按商品+仓库+批次号+色号查询现有库存记录（batchNo/colorNo为null时匹配IS NULL记录）
        LambdaQueryWrapper<WmsInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmsInventory::getProductId, productId)
                .eq(WmsInventory::getWarehouseId, warehouseId)
                .eq(batchNo != null, WmsInventory::getBatchNo, batchNo)
                .isNull(batchNo == null, WmsInventory::getBatchNo)
                .eq(colorNo != null, WmsInventory::getColorNo, colorNo)
                .isNull(colorNo == null, WmsInventory::getColorNo);
        WmsInventory inventory = wmsInventoryMapper.selectOne(wrapper);

        if (inventory != null) {
            // 库存记录已存在，累加数量
            LambdaUpdateWrapper<WmsInventory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(WmsInventory::getId, inventory.getId())
                    .set(WmsInventory::getQuantity, inventory.getQuantity() + quantity);
            wmsInventoryMapper.update(null, updateWrapper);
        } else {
            // 库存记录不存在，新增记录
            WmsInventory newInventory = new WmsInventory();
            newInventory.setProductId(productId);
            newInventory.setWarehouseId(warehouseId);
            newInventory.setBatchNo(batchNo);
            newInventory.setColorNo(colorNo);
            newInventory.setQuantity(quantity);
            wmsInventoryMapper.insert(newInventory);
        }
    }

    /**
     * 扣减库存
     * 按 商品+仓库+批次号+色号 维度匹配库存记录，存在且数量充足则扣减，不足则抛出BusinessException
     *
     * @param productId   商品ID
     * @param warehouseId 仓库ID
     * @param batchNo     批次号
     * @param colorNo     色号
     * @param quantity    扣减数量，必须大于0
     * @throws BusinessException 库存记录不存在或库存不足时抛出
     */
    @Override
    public void decreaseStock(Long productId, Long warehouseId, String batchNo, String colorNo, Integer quantity) {
        // 按商品+仓库+批次号+色号查询现有库存记录（batchNo/colorNo为null时匹配IS NULL记录）
        LambdaQueryWrapper<WmsInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmsInventory::getProductId, productId)
                .eq(WmsInventory::getWarehouseId, warehouseId)
                .eq(batchNo != null, WmsInventory::getBatchNo, batchNo)
                .isNull(batchNo == null, WmsInventory::getBatchNo)
                .eq(colorNo != null, WmsInventory::getColorNo, colorNo)
                .isNull(colorNo == null, WmsInventory::getColorNo);
        WmsInventory inventory = wmsInventoryMapper.selectOne(wrapper);

        if (inventory == null) {
            throw new BusinessException("库存记录不存在，商品ID：" + productId + "，仓库ID：" + warehouseId);
        }
        if (inventory.getQuantity() < quantity) {
            throw new BusinessException("库存不足，当前库存：" + inventory.getQuantity() + "，需要扣减：" + quantity);
        }

        // 扣减库存
        LambdaUpdateWrapper<WmsInventory> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WmsInventory::getId, inventory.getId())
                .set(WmsInventory::getQuantity, inventory.getQuantity() - quantity);
        wmsInventoryMapper.update(null, updateWrapper);
    }
}
