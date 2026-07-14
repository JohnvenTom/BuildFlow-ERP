package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.common.utils.OrderNoGenerator;
import com.buildflow.erp.entity.WmsStockTransfer;
import com.buildflow.erp.entity.WmsStockTransferItem;
import com.buildflow.erp.mapper.WmsStockTransferItemMapper;
import com.buildflow.erp.mapper.WmsStockTransferMapper;
import com.buildflow.erp.service.WmsInventoryService;
import com.buildflow.erp.service.WmsStockTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 调拨单服务实现类
 * 实现仓库间库存调拨的核心业务逻辑，包括分页查询、新增调拨单（含明细）、审核调拨单、作废调拨单等操作。
 * 审核调拨单时通过事务保证扣减调出仓库存和增加调入仓库存的原子性。
 */
@Service
public class WmsStockTransferServiceImpl implements WmsStockTransferService {

    @Autowired
    private WmsStockTransferMapper wmsStockTransferMapper;

    @Autowired
    private WmsStockTransferItemMapper wmsStockTransferItemMapper;

    @Autowired
    private WmsInventoryService wmsInventoryService;

    /**
     * 分页查询调拨单列表
     * 支持按调拨单号模糊搜索、单据状态过滤，按创建时间倒序排列
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param transferNo  调拨单号（模糊查询），可为null
     * @param status      单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页调拨单列表
     */
    @Override
    public R<PageResult<WmsStockTransfer>> page(Integer pageNum, Integer pageSize, String transferNo, String status) {
        Page<WmsStockTransfer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WmsStockTransfer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(transferNo), WmsStockTransfer::getTransferNo, transferNo)
                .eq(StringUtils.hasText(status), WmsStockTransfer::getStatus, status)
                .orderByDesc(WmsStockTransfer::getCreateTime);
        Page<WmsStockTransfer> result = wmsStockTransferMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    /**
     * 新增调拨单（含明细）
     * 自动生成调拨单号，设置初始状态为草稿，插入主表和明细表
     *
     * @param transfer 调拨单主表对象，需包含fromWarehouseId、toWarehouseId等字段
     * @param items    调拨单明细列表，每条需包含productId和quantity
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> add(WmsStockTransfer transfer, List<WmsStockTransferItem> items) {
        // 生成调拨单号并设置初始状态
        transfer.setTransferNo(OrderNoGenerator.generate(OrderNoGenerator.STOCK_TRANSFER));
        transfer.setStatus(Constants.ORDER_STATUS_DRAFT);
        wmsStockTransferMapper.insert(transfer);

        // 插入调拨单明细
        if (items != null && !items.isEmpty()) {
            for (WmsStockTransferItem item : items) {
                item.setTransferId(transfer.getId());
                wmsStockTransferItemMapper.insert(item);
            }
        }
        return R.ok();
    }

    /**
     * 审核调拨单
     * 事务操作：校验单据状态为草稿，更新状态为已审核，逐条明细扣减调出仓库存并增加调入仓库存。
     * 注意：调拨单明细未指定批次号和色号，使用null值操作无批次库存。
     *
     * @param id      调拨单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     * @throws com.buildflow.erp.common.exception.BusinessException 调出仓库存不足时抛出，事务回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> audit(Long id, Long auditBy) {
        WmsStockTransfer transfer = wmsStockTransferMapper.selectById(id);
        if (transfer == null) {
            return R.fail("调拨单不存在");
        }
        if (!Constants.ORDER_STATUS_DRAFT.equals(transfer.getStatus())) {
            return R.fail("调拨单状态不允许审核");
        }

        // 查询调拨单明细
        LambdaQueryWrapper<WmsStockTransferItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(WmsStockTransferItem::getTransferId, id);
        List<WmsStockTransferItem> items = wmsStockTransferItemMapper.selectList(itemWrapper);

        // 逐条明细扣减调出仓库存并增加调入仓库存
        for (WmsStockTransferItem item : items) {
            // 扣减调出仓库存（库存不足将抛出BusinessException，触发事务回滚）
            wmsInventoryService.decreaseStock(item.getProductId(), transfer.getFromWarehouseId(),
                    null, null, item.getQuantity());
            // 增加调入仓库存
            wmsInventoryService.increaseStock(item.getProductId(), transfer.getToWarehouseId(),
                    null, null, item.getQuantity());
        }

        // 更新调拨单状态为已审核
        LambdaUpdateWrapper<WmsStockTransfer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WmsStockTransfer::getId, id)
                .set(WmsStockTransfer::getStatus, Constants.ORDER_STATUS_AUDITED)
                .set(WmsStockTransfer::getAuditBy, auditBy)
                .set(WmsStockTransfer::getAuditTime, LocalDateTime.now());
        wmsStockTransferMapper.update(null, updateWrapper);

        return R.ok();
    }

    /**
     * 作废调拨单
     * 仅草稿状态或已审核状态的调拨单可作废，已审核单据作废不回退库存
     *
     * @param id 调拨单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    @Override
    public R<Void> voidOrder(Long id) {
        WmsStockTransfer transfer = wmsStockTransferMapper.selectById(id);
        if (transfer == null) {
            return R.fail("调拨单不存在");
        }
        if (Constants.ORDER_STATUS_VOID.equals(transfer.getStatus())) {
            return R.fail("调拨单已作废，不可重复操作");
        }

        // 更新调拨单状态为已作废
        LambdaUpdateWrapper<WmsStockTransfer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WmsStockTransfer::getId, id)
                .set(WmsStockTransfer::getStatus, Constants.ORDER_STATUS_VOID);
        wmsStockTransferMapper.update(null, updateWrapper);

        return R.ok();
    }
}
