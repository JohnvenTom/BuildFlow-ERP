package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.WmsInventory;
import com.buildflow.erp.entity.WmsProduct;
import com.buildflow.erp.entity.WmsWarehouse;
import com.buildflow.erp.mapper.WmsInventoryMapper;
import com.buildflow.erp.mapper.WmsProductMapper;
import com.buildflow.erp.mapper.WmsWarehouseMapper;
import com.buildflow.erp.service.WmsAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仓储预警服务实现类
 * 实现库存预警的核心业务逻辑，包括低库存预警和呆滞库存预警。
 * 低库存预警：查询各商品在所有仓库的库存总量低于最低库存阈值的记录。
 * 呆滞库存预警：查询库存更新时间超过90天无出库变动的库存记录。
 */
@Service
public class WmsAlertServiceImpl implements WmsAlertService {

    @Autowired
    private WmsProductMapper wmsProductMapper;

    @Autowired
    private WmsWarehouseMapper wmsWarehouseMapper;

    @Autowired
    private WmsInventoryMapper wmsInventoryMapper;

    /**
     * 低库存预警
     * 查询所有设置了最低库存阈值的商品，统计各商品在所有仓库的库存总量，
     * 库存总量低于最低库存阈值的商品加入预警列表。
     *
     * @return 预警列表，每条记录包含商品ID、商品名称、分类、当前总库存、最低库存等字段
     */
    @Override
    public R<List<Map<String, Object>>> lowStockAlert() {
        // 查询所有设置了最低库存阈值的商品
        LambdaQueryWrapper<WmsProduct> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.isNotNull(WmsProduct::getMinStock)
                .gt(WmsProduct::getMinStock, 0);
        List<WmsProduct> products = wmsProductMapper.selectList(productWrapper);

        // 查询所有库存记录
        List<WmsInventory> allInventory = wmsInventoryMapper.selectList(null);

        // 按商品ID分组汇总库存数量
        Map<Long, Integer> stockMap = allInventory.stream()
                .collect(Collectors.groupingBy(
                        WmsInventory::getProductId,
                        Collectors.summingInt(WmsInventory::getQuantity)
                ));

        // 筛选库存不足的商品
        List<Map<String, Object>> alertList = new ArrayList<>();
        for (WmsProduct product : products) {
            Integer currentStock = stockMap.getOrDefault(product.getId(), 0);
            if (currentStock < product.getMinStock()) {
                Map<String, Object> map = new HashMap<>();
                map.put("productId", product.getId());
                map.put("productName", product.getName());
                map.put("category", product.getCategory());
                map.put("spec", product.getSpec());
                map.put("unit", product.getUnit());
                map.put("currentStock", currentStock);
                map.put("minStock", product.getMinStock());
                map.put("shortage", product.getMinStock() - currentStock);
                alertList.add(map);
            }
        }
        return R.ok(alertList);
    }

    /**
     * 呆滞库存预警
     * 查询库存更新时间超过90天无出库变动的库存记录，关联商品和仓库信息返回。
     *
     * @return 预警列表，每条记录包含商品ID、商品名称、仓库ID、仓库名称、批次号、色号、库存数量、最后更新时间等字段
     */
    @Override
    public R<List<Map<String, Object>>> dormantStockAlert() {
        // 查询90天前未变动的库存记录
        LocalDateTime threshold = LocalDateTime.now().minusDays(90);
        LambdaQueryWrapper<WmsInventory> inventoryWrapper = new LambdaQueryWrapper<>();
        inventoryWrapper.lt(WmsInventory::getUpdateTime, threshold);
        List<WmsInventory> dormantInventory = wmsInventoryMapper.selectList(inventoryWrapper);

        if (dormantInventory.isEmpty()) {
            return R.ok(new ArrayList<>());
        }

        // 批量查询关联的商品信息
        List<Long> productIds = dormantInventory.stream()
                .map(WmsInventory::getProductId)
                .distinct()
                .collect(Collectors.toList());
        LambdaQueryWrapper<WmsProduct> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.in(WmsProduct::getId, productIds);
        List<WmsProduct> products = wmsProductMapper.selectList(productWrapper);
        Map<Long, WmsProduct> productMap = products.stream()
                .collect(Collectors.toMap(WmsProduct::getId, p -> p));

        // 批量查询关联的仓库信息
        List<Long> warehouseIds = dormantInventory.stream()
                .map(WmsInventory::getWarehouseId)
                .distinct()
                .collect(Collectors.toList());
        LambdaQueryWrapper<WmsWarehouse> warehouseWrapper = new LambdaQueryWrapper<>();
        warehouseWrapper.in(WmsWarehouse::getId, warehouseIds);
        List<WmsWarehouse> warehouses = wmsWarehouseMapper.selectList(warehouseWrapper);
        Map<Long, WmsWarehouse> warehouseMap = warehouses.stream()
                .collect(Collectors.toMap(WmsWarehouse::getId, w -> w));

        // 构建预警列表
        List<Map<String, Object>> alertList = new ArrayList<>();
        for (WmsInventory inventory : dormantInventory) {
            Map<String, Object> map = new HashMap<>();
            map.put("inventoryId", inventory.getId());
            map.put("productId", inventory.getProductId());
            WmsProduct product = productMap.get(inventory.getProductId());
            map.put("productName", product != null ? product.getName() : null);
            map.put("spec", product != null ? product.getSpec() : null);
            map.put("warehouseId", inventory.getWarehouseId());
            WmsWarehouse warehouse = warehouseMap.get(inventory.getWarehouseId());
            map.put("warehouseName", warehouse != null ? warehouse.getName() : null);
            map.put("batchNo", inventory.getBatchNo());
            map.put("colorNo", inventory.getColorNo());
            map.put("quantity", inventory.getQuantity());
            map.put("lastUpdateTime", inventory.getUpdateTime());
            map.put("dormantDays", java.time.Duration.between(inventory.getUpdateTime(), LocalDateTime.now()).toDays());
            alertList.add(map);
        }
        return R.ok(alertList);
    }
}
