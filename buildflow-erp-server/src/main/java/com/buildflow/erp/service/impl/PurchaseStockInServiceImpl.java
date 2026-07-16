package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.common.utils.OrderNoGenerator;
import com.buildflow.erp.entity.CrmSupplier;
import com.buildflow.erp.entity.PurchaseStockIn;
import com.buildflow.erp.entity.PurchaseStockInItem;
import com.buildflow.erp.entity.WmsWarehouse;
import com.buildflow.erp.mapper.CrmSupplierMapper;
import com.buildflow.erp.mapper.PurchaseStockInItemMapper;
import com.buildflow.erp.mapper.PurchaseStockInMapper;
import com.buildflow.erp.mapper.WmsWarehouseMapper;
import com.buildflow.erp.service.PurchaseStockInService;
import com.buildflow.erp.service.WmsInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 采购入库单服务实现类
 * 实现采购入库管理的核心业务逻辑，包括分页查询、新增入库单（含明细）、审核入库单、作废入库单等操作。
 * 审核入库单时通过事务保证增加库存的原子性，并预留供应商应付台账生成接口。
 */
@Service
public class PurchaseStockInServiceImpl implements PurchaseStockInService {

    @Autowired
    private PurchaseStockInMapper purchaseStockInMapper;

    @Autowired
    private PurchaseStockInItemMapper purchaseStockInItemMapper;

    @Autowired
    private WmsInventoryService wmsInventoryService;

    @Autowired
    private CrmSupplierMapper crmSupplierMapper;

    @Autowired
    private WmsWarehouseMapper wmsWarehouseMapper;

    /**
     * 分页查询入库单列表
     * 支持按入库单号模糊搜索、供应商过滤、单据状态过滤，按创建时间倒序排列
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param stockInNo   入库单号（模糊查询），可为null
     * @param supplierId  供应商ID，可为null表示不过滤
     * @param status      单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页入库单列表
     */
    @Override
    public R<PageResult<PurchaseStockIn>> page(Integer pageNum, Integer pageSize, String stockInNo, Long supplierId, String status) {
        Page<PurchaseStockIn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseStockIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(stockInNo), PurchaseStockIn::getStockInNo, stockInNo)
                .eq(supplierId != null, PurchaseStockIn::getSupplierId, supplierId)
                .eq(StringUtils.hasText(status), PurchaseStockIn::getStatus, status)
                .orderByDesc(PurchaseStockIn::getCreateTime);
        Page<PurchaseStockIn> result = purchaseStockInMapper.selectPage(page, wrapper);
        List<PurchaseStockIn> records = result.getRecords();
        // 批量查询供应商名称并填充到结果中
        List<Long> supplierIds = records.stream()
                .map(PurchaseStockIn::getSupplierId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!supplierIds.isEmpty()) {
            List<CrmSupplier> suppliers = crmSupplierMapper.selectBatchIds(supplierIds);
            Map<Long, String> supplierMap = suppliers.stream()
                    .collect(Collectors.toMap(CrmSupplier::getId, CrmSupplier::getName));
            records.forEach(e -> e.setSupplierName(supplierMap.get(e.getSupplierId())));
        }
        // 批量查询入库仓库名称并填充到结果中
        List<Long> warehouseIds = records.stream()
                .map(PurchaseStockIn::getWarehouseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!warehouseIds.isEmpty()) {
            List<WmsWarehouse> warehouses = wmsWarehouseMapper.selectBatchIds(warehouseIds);
            Map<Long, String> warehouseMap = warehouses.stream()
                    .collect(Collectors.toMap(WmsWarehouse::getId, WmsWarehouse::getName));
            records.forEach(e -> e.setWarehouseName(warehouseMap.get(e.getWarehouseId())));
        }
        return R.ok(new PageResult<>(result.getTotal(), records));
    }

    /**
     * 新增入库单（含明细）
     * 自动生成入库单号（SI+日期+流水号），设置初始状态为草稿，插入主表和明细表
     *
     * @param stockIn 入库单主表对象，需包含orderId、supplierId、warehouseId等字段
     * @param items   入库单明细列表，每条需包含productId、batchNo、quantity、unitPrice
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> add(PurchaseStockIn stockIn, List<PurchaseStockInItem> items) {
        // 生成入库单号并设置初始状态
        stockIn.setStockInNo(OrderNoGenerator.generate(OrderNoGenerator.STOCK_IN));
        stockIn.setStatus(Constants.ORDER_STATUS_DRAFT);
        purchaseStockInMapper.insert(stockIn);

        // 插入入库单明细
        if (items != null && !items.isEmpty()) {
            for (PurchaseStockInItem item : items) {
                item.setStockInId(stockIn.getId());
                purchaseStockInItemMapper.insert(item);
            }
        }
        return R.ok();
    }

    /**
     * 审核入库单
     * 事务操作：校验单据状态为草稿，逐条明细调用increaseStock增加库存，更新状态为已审核，生成供应商应付台账。
     * 注意：应付台账生成功能待FinPayableService完成后集成。
     *
     * @param id      入库单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> audit(Long id, Long auditBy) {
        PurchaseStockIn stockIn = purchaseStockInMapper.selectById(id);
        if (stockIn == null) {
            return R.fail("入库单不存在");
        }
        if (!Constants.ORDER_STATUS_DRAFT.equals(stockIn.getStatus())) {
            return R.fail("入库单状态不允许审核");
        }

        // 查询入库单明细
        LambdaQueryWrapper<PurchaseStockInItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(PurchaseStockInItem::getStockInId, id);
        List<PurchaseStockInItem> items = purchaseStockInItemMapper.selectList(itemWrapper);

        // 逐条明细增加库存
        for (PurchaseStockInItem item : items) {
            wmsInventoryService.increaseStock(item.getProductId(), stockIn.getWarehouseId(),
                    item.getBatchNo(), item.getColorNo(), item.getQuantity());
        }

        // 更新入库单状态为已审核
        LambdaUpdateWrapper<PurchaseStockIn> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PurchaseStockIn::getId, id)
                .set(PurchaseStockIn::getStatus, Constants.ORDER_STATUS_AUDITED)
                .set(PurchaseStockIn::getAuditBy, auditBy)
                .set(PurchaseStockIn::getAuditTime, LocalDateTime.now());
        purchaseStockInMapper.update(null, updateWrapper);

        // TODO: 待FinPayableService完成后，生成供应商应付台账
        // finPayableService.createPayable(stockIn.getSupplierId(), stockIn.getStockInNo(), stockIn.getTotalAmount());

        return R.ok();
    }

    /**
     * 作废入库单
     * 仅草稿状态的入库单可作废
     *
     * @param id 入库单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    @Override
    public R<Void> voidOrder(Long id) {
        PurchaseStockIn stockIn = purchaseStockInMapper.selectById(id);
        if (stockIn == null) {
            return R.fail("入库单不存在");
        }
        if (Constants.ORDER_STATUS_VOID.equals(stockIn.getStatus())) {
            return R.fail("入库单已作废，不可重复操作");
        }

        // 更新入库单状态为已作废
        LambdaUpdateWrapper<PurchaseStockIn> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PurchaseStockIn::getId, id)
                .set(PurchaseStockIn::getStatus, Constants.ORDER_STATUS_VOID);
        purchaseStockInMapper.update(null, updateWrapper);

        return R.ok();
    }
}
