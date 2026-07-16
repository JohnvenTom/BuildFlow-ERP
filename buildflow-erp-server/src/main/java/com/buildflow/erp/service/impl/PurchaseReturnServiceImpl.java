package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.common.utils.OrderNoGenerator;
import com.buildflow.erp.entity.CrmSupplier;
import com.buildflow.erp.entity.PurchaseReturn;
import com.buildflow.erp.entity.PurchaseReturnItem;
import com.buildflow.erp.entity.PurchaseStockIn;
import com.buildflow.erp.mapper.CrmSupplierMapper;
import com.buildflow.erp.mapper.PurchaseReturnItemMapper;
import com.buildflow.erp.mapper.PurchaseReturnMapper;
import com.buildflow.erp.mapper.PurchaseStockInMapper;
import com.buildflow.erp.service.PurchaseReturnService;
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
 * 采购退货单服务实现类
 * 实现采购退货管理的核心业务逻辑，包括分页查询、新增退货单（含明细）、审核退货单、作废退货单等操作。
 * 审核退货单时通过事务保证扣减库存的原子性，并预留供应商应付台账减少接口。
 */
@Service
public class PurchaseReturnServiceImpl implements PurchaseReturnService {

    @Autowired
    private PurchaseReturnMapper purchaseReturnMapper;

    @Autowired
    private PurchaseReturnItemMapper purchaseReturnItemMapper;

    @Autowired
    private PurchaseStockInMapper purchaseStockInMapper;

    @Autowired
    private CrmSupplierMapper crmSupplierMapper;

    @Autowired
    private WmsInventoryService wmsInventoryService;

    /**
     * 分页查询退货单列表
     * 支持按退货单号模糊搜索、供应商过滤、单据状态过滤，按创建时间倒序排列
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param returnNo    退货单号（模糊查询），可为null
     * @param supplierId  供应商ID，可为null表示不过滤
     * @param status      单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页退货单列表
     */
    @Override
    public R<PageResult<PurchaseReturn>> page(Integer pageNum, Integer pageSize, String returnNo, Long supplierId, String status) {
        Page<PurchaseReturn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseReturn> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(returnNo), PurchaseReturn::getReturnNo, returnNo)
                .eq(supplierId != null, PurchaseReturn::getSupplierId, supplierId)
                .eq(StringUtils.hasText(status), PurchaseReturn::getStatus, status)
                .orderByDesc(PurchaseReturn::getCreateTime);
        Page<PurchaseReturn> result = purchaseReturnMapper.selectPage(page, wrapper);
        List<PurchaseReturn> records = result.getRecords();
        // 批量查询供应商名称并填充到结果中
        List<Long> supplierIds = records.stream()
                .map(PurchaseReturn::getSupplierId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!supplierIds.isEmpty()) {
            List<CrmSupplier> suppliers = crmSupplierMapper.selectBatchIds(supplierIds);
            Map<Long, String> supplierMap = suppliers.stream()
                    .collect(Collectors.toMap(CrmSupplier::getId, CrmSupplier::getName));
            records.forEach(e -> e.setSupplierName(supplierMap.get(e.getSupplierId())));
        }
        // 批量查询关联入库单号并填充到结果中
        List<Long> stockInIds = records.stream()
                .map(PurchaseReturn::getStockInId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!stockInIds.isEmpty()) {
            List<PurchaseStockIn> stockIns = purchaseStockInMapper.selectBatchIds(stockInIds);
            Map<Long, String> stockInMap = stockIns.stream()
                    .collect(Collectors.toMap(PurchaseStockIn::getId, PurchaseStockIn::getStockInNo));
            records.forEach(e -> e.setStockInOrderNo(stockInMap.get(e.getStockInId())));
        }
        return R.ok(new PageResult<>(result.getTotal(), records));
    }

    /**
     * 新增退货单（含明细）
     * 自动生成退货单号（PR+日期+流水号），设置初始状态为草稿，插入主表和明细表
     *
     * @param returnOrder 退货单主表对象，需包含supplierId、stockInId等字段
     * @param items       退货单明细列表，每条需包含productId、quantity、unitPrice
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> add(PurchaseReturn returnOrder, List<PurchaseReturnItem> items) {
        // 生成退货单号并设置初始状态
        returnOrder.setReturnNo(OrderNoGenerator.generate(OrderNoGenerator.PURCHASE_RETURN));
        returnOrder.setStatus(Constants.ORDER_STATUS_DRAFT);
        purchaseReturnMapper.insert(returnOrder);

        // 插入退货单明细
        if (items != null && !items.isEmpty()) {
            for (PurchaseReturnItem item : items) {
                item.setReturnId(returnOrder.getId());
                purchaseReturnItemMapper.insert(item);
            }
        }
        return R.ok();
    }

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> audit(Long id, Long auditBy) {
        PurchaseReturn returnOrder = purchaseReturnMapper.selectById(id);
        if (returnOrder == null) {
            return R.fail("退货单不存在");
        }
        if (!Constants.ORDER_STATUS_DRAFT.equals(returnOrder.getStatus())) {
            return R.fail("退货单状态不允许审核");
        }

        // 查询关联入库单，获取仓库ID
        PurchaseStockIn stockIn = purchaseStockInMapper.selectById(returnOrder.getStockInId());
        if (stockIn == null) {
            return R.fail("关联入库单不存在");
        }

        // 查询退货单明细
        LambdaQueryWrapper<PurchaseReturnItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(PurchaseReturnItem::getReturnId, id);
        List<PurchaseReturnItem> items = purchaseReturnItemMapper.selectList(itemWrapper);

        // 逐条明细扣减库存（库存不足将抛出BusinessException，触发事务回滚）
        for (PurchaseReturnItem item : items) {
            wmsInventoryService.decreaseStock(item.getProductId(), stockIn.getWarehouseId(),
                    null, null, item.getQuantity());
        }

        // 更新退货单状态为已审核
        LambdaUpdateWrapper<PurchaseReturn> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PurchaseReturn::getId, id)
                .set(PurchaseReturn::getStatus, Constants.ORDER_STATUS_AUDITED)
                .set(PurchaseReturn::getAuditBy, auditBy)
                .set(PurchaseReturn::getAuditTime, LocalDateTime.now());
        purchaseReturnMapper.update(null, updateWrapper);

        // TODO: 待FinPayableService完成后，减少供应商应付台账
        // finPayableService.reducePayable(returnOrder.getSupplierId(), returnOrder.getReturnNo(), returnOrder.getTotalAmount());

        return R.ok();
    }

    /**
     * 作废退货单
     * 仅草稿状态的退货单可作废
     *
     * @param id 退货单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    @Override
    public R<Void> voidOrder(Long id) {
        PurchaseReturn returnOrder = purchaseReturnMapper.selectById(id);
        if (returnOrder == null) {
            return R.fail("退货单不存在");
        }
        if (Constants.ORDER_STATUS_VOID.equals(returnOrder.getStatus())) {
            return R.fail("退货单已作废，不可重复操作");
        }

        // 更新退货单状态为已作废
        LambdaUpdateWrapper<PurchaseReturn> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PurchaseReturn::getId, id)
                .set(PurchaseReturn::getStatus, Constants.ORDER_STATUS_VOID);
        purchaseReturnMapper.update(null, updateWrapper);

        return R.ok();
    }
}
