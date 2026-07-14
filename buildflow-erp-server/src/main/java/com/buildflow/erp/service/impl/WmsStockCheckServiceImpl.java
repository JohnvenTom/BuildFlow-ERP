package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.common.utils.OrderNoGenerator;
import com.buildflow.erp.entity.WmsStockCheck;
import com.buildflow.erp.entity.WmsStockCheckItem;
import com.buildflow.erp.mapper.WmsStockCheckItemMapper;
import com.buildflow.erp.mapper.WmsStockCheckMapper;
import com.buildflow.erp.service.WmsInventoryService;
import com.buildflow.erp.service.WmsStockCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 盘点单服务实现类
 * 实现仓库库存盘点的核心业务逻辑，包括分页查询、新增盘点单（含明细）、审核盘点单、作废盘点单等操作。
 * 审核盘点单时通过事务保证根据盘盈盘亏修正库存的原子性。
 */
@Service
public class WmsStockCheckServiceImpl implements WmsStockCheckService {

    @Autowired
    private WmsStockCheckMapper wmsStockCheckMapper;

    @Autowired
    private WmsStockCheckItemMapper wmsStockCheckItemMapper;

    @Autowired
    private WmsInventoryService wmsInventoryService;

    /**
     * 分页查询盘点单列表
     * 支持按盘点单号模糊搜索、单据状态过滤，按创建时间倒序排列
     *
     * @param pageNum   当前页码，从1开始
     * @param pageSize  每页记录数
     * @param checkNo   盘点单号（模糊查询），可为null
     * @param status    单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页盘点单列表
     */
    @Override
    public R<PageResult<WmsStockCheck>> page(Integer pageNum, Integer pageSize, String checkNo, String status) {
        Page<WmsStockCheck> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WmsStockCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(checkNo), WmsStockCheck::getCheckNo, checkNo)
                .eq(StringUtils.hasText(status), WmsStockCheck::getStatus, status)
                .orderByDesc(WmsStockCheck::getCreateTime);
        Page<WmsStockCheck> result = wmsStockCheckMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    /**
     * 新增盘点单（含明细）
     * 自动生成盘点单号，设置初始状态为草稿，自动计算差异数量（actualQty - systemQty），插入主表和明细表
     *
     * @param check 盘点单主表对象，需包含warehouseId等字段
     * @param items 盘点单明细列表，每条需包含productId、systemQty、actualQty
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> add(WmsStockCheck check, List<WmsStockCheckItem> items) {
        // 生成盘点单号并设置初始状态
        check.setCheckNo(OrderNoGenerator.generate(OrderNoGenerator.STOCK_CHECK));
        check.setStatus(Constants.ORDER_STATUS_DRAFT);
        wmsStockCheckMapper.insert(check);

        // 插入盘点单明细，计算差异数量
        if (items != null && !items.isEmpty()) {
            for (WmsStockCheckItem item : items) {
                item.setCheckId(check.getId());
                // 计算差异数量 = 实际库存 - 系统库存
                int systemQty = item.getSystemQty() != null ? item.getSystemQty() : 0;
                int actualQty = item.getActualQty() != null ? item.getActualQty() : 0;
                item.setDiffQty(actualQty - systemQty);
                wmsStockCheckItemMapper.insert(item);
            }
        }
        return R.ok();
    }

    /**
     * 审核盘点单
     * 事务操作：校验单据状态为草稿，更新状态为已审核，根据差异修正库存（盘盈增加库存，盘亏扣减库存）。
     * 注意：盘点单明细未指定批次号和色号，使用null值操作无批次库存。
     *
     * @param id      盘点单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     * @throws com.buildflow.erp.common.exception.BusinessException 盘亏时库存不足抛出，事务回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> audit(Long id, Long auditBy) {
        WmsStockCheck check = wmsStockCheckMapper.selectById(id);
        if (check == null) {
            return R.fail("盘点单不存在");
        }
        if (!Constants.ORDER_STATUS_DRAFT.equals(check.getStatus())) {
            return R.fail("盘点单状态不允许审核");
        }

        // 查询盘点单明细
        LambdaQueryWrapper<WmsStockCheckItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(WmsStockCheckItem::getCheckId, id);
        List<WmsStockCheckItem> items = wmsStockCheckItemMapper.selectList(itemWrapper);

        // 根据差异修正库存
        for (WmsStockCheckItem item : items) {
            int diffQty = item.getDiffQty() != null ? item.getDiffQty() : 0;
            if (diffQty > 0) {
                // 盘盈：增加库存
                wmsInventoryService.increaseStock(item.getProductId(), check.getWarehouseId(),
                        null, null, diffQty);
            } else if (diffQty < 0) {
                // 盘亏：扣减库存（库存不足将抛出BusinessException，触发事务回滚）
                wmsInventoryService.decreaseStock(item.getProductId(), check.getWarehouseId(),
                        null, null, Math.abs(diffQty));
            }
        }

        // 更新盘点单状态为已审核
        LambdaUpdateWrapper<WmsStockCheck> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WmsStockCheck::getId, id)
                .set(WmsStockCheck::getStatus, Constants.ORDER_STATUS_AUDITED)
                .set(WmsStockCheck::getAuditBy, auditBy)
                .set(WmsStockCheck::getAuditTime, LocalDateTime.now());
        wmsStockCheckMapper.update(null, updateWrapper);

        return R.ok();
    }

    /**
     * 作废盘点单
     * 仅草稿状态的盘点单可作废
     *
     * @param id 盘点单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    @Override
    public R<Void> voidOrder(Long id) {
        WmsStockCheck check = wmsStockCheckMapper.selectById(id);
        if (check == null) {
            return R.fail("盘点单不存在");
        }
        if (Constants.ORDER_STATUS_VOID.equals(check.getStatus())) {
            return R.fail("盘点单已作废，不可重复操作");
        }

        // 更新盘点单状态为已作废
        LambdaUpdateWrapper<WmsStockCheck> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WmsStockCheck::getId, id)
                .set(WmsStockCheck::getStatus, Constants.ORDER_STATUS_VOID);
        wmsStockCheckMapper.update(null, updateWrapper);

        return R.ok();
    }
}
