package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.common.utils.OrderNoGenerator;
import com.buildflow.erp.entity.FinPaymentPay;
import com.buildflow.erp.entity.FinPaymentPayItem;
import com.buildflow.erp.entity.FinPayable;
import com.buildflow.erp.mapper.FinPayableMapper;
import com.buildflow.erp.mapper.FinPaymentPayItemMapper;
import com.buildflow.erp.mapper.FinPaymentPayMapper;
import com.buildflow.erp.service.FinPaymentPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商付款单服务实现类
 * 实现付款单管理的核心业务逻辑，包括分页查询、新增付款单（含核销明细）、审核付款单、作废付款单等操作。
 * 审核时通过事务核销应付台账，保证数据一致性。
 */
@Service
public class FinPaymentPayServiceImpl implements FinPaymentPayService {

    @Autowired
    private FinPaymentPayMapper finPaymentPayMapper;

    @Autowired
    private FinPaymentPayItemMapper finPaymentPayItemMapper;

    @Autowired
    private FinPayableMapper finPayableMapper;

    /**
     * 分页查询付款单列表
     * 支持按付款单号模糊搜索、供应商ID、状态过滤，按创建时间倒序排列
     *
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param payNo      付款单号（模糊查询），可为null
     * @param supplierId 供应商ID，可为null表示不过滤
     * @param status     单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页付款单列表
     */
    @Override
    public R<PageResult<FinPaymentPay>> page(Integer pageNum, Integer pageSize, String payNo, Long supplierId, String status) {
        Page<FinPaymentPay> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FinPaymentPay> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(payNo), FinPaymentPay::getPayNo, payNo)
                .eq(supplierId != null, FinPaymentPay::getSupplierId, supplierId)
                .eq(StringUtils.hasText(status), FinPaymentPay::getStatus, status)
                .orderByDesc(FinPaymentPay::getCreateTime);
        Page<FinPaymentPay> result = finPaymentPayMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    /**
     * 新增付款单及核销明细
     * 自动生成付款单号（PP+日期+流水号），设置初始状态为草稿，
     * 校验核销明细总金额不超过付款总金额，插入主表和明细表
     *
     * @param pay   付款单主表对象，需包含supplierId、amount、payMethod等字段
     * @param items 核销明细列表，每条需包含payableId、amount
     * @return 操作结果，核销明细总金额超过付款金额时返回R.fail()
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> add(FinPaymentPay pay, List<FinPaymentPayItem> items) {
        // 生成付款单号并设置初始状态
        pay.setPayNo(OrderNoGenerator.generate(OrderNoGenerator.PAYMENT_PAY));
        pay.setStatus(Constants.ORDER_STATUS_DRAFT);

        // 校验核销明细总金额不超过付款总金额
        if (items != null && !items.isEmpty()) {
            BigDecimal totalItemAmount = BigDecimal.ZERO;
            for (FinPaymentPayItem item : items) {
                totalItemAmount = totalItemAmount.add(item.getAmount());
            }
            if (totalItemAmount.compareTo(pay.getAmount()) > 0) {
                return R.fail("核销明细总金额超过付款总金额");
            }
        }

        // 插入付款单主表
        finPaymentPayMapper.insert(pay);

        // 插入核销明细
        if (items != null && !items.isEmpty()) {
            for (FinPaymentPayItem item : items) {
                item.setPayId(pay.getId());
                finPaymentPayItemMapper.insert(item);
            }
        }
        return R.ok();
    }

    /**
     * 审核付款单
     * 事务操作：校验单据状态为草稿，逐条明细核销应付台账（增加paidAmount、减少remainAmount），
     * 若剩余金额为0则更新状态为已结清，更新付款单状态为已审核。
     *
     * @param id      付款单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> audit(Long id, Long auditBy) {
        FinPaymentPay pay = finPaymentPayMapper.selectById(id);
        if (pay == null) {
            return R.fail("付款单不存在");
        }
        if (!Constants.ORDER_STATUS_DRAFT.equals(pay.getStatus())) {
            return R.fail("付款单状态不允许审核");
        }

        // 查询核销明细
        LambdaQueryWrapper<FinPaymentPayItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(FinPaymentPayItem::getPayId, id);
        List<FinPaymentPayItem> items = finPaymentPayItemMapper.selectList(itemWrapper);

        // 逐条明细核销应付台账
        for (FinPaymentPayItem item : items) {
            FinPayable payable = finPayableMapper.selectById(item.getPayableId());
            if (payable == null) {
                return R.fail("应付台账记录不存在，ID：" + item.getPayableId());
            }
            // 校验核销金额不超过剩余应付金额
            if (item.getAmount().compareTo(payable.getRemainAmount()) > 0) {
                return R.fail("核销金额超过应付剩余金额，应付台账ID：" + item.getPayableId());
            }
            // 更新应付台账已付款和剩余金额
            BigDecimal newPaidAmount = payable.getPaidAmount().add(item.getAmount());
            BigDecimal newRemainAmount = payable.getRemainAmount().subtract(item.getAmount());
            LambdaUpdateWrapper<FinPayable> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(FinPayable::getId, payable.getId())
                    .set(FinPayable::getPaidAmount, newPaidAmount)
                    .set(FinPayable::getRemainAmount, newRemainAmount);
            // 剩余金额为0时更新状态为已结清
            if (newRemainAmount.compareTo(BigDecimal.ZERO) == 0) {
                updateWrapper.set(FinPayable::getStatus, Constants.FIN_STATUS_SETTLED);
            }
            finPayableMapper.update(null, updateWrapper);
        }

        // 更新付款单状态为已审核
        LambdaUpdateWrapper<FinPaymentPay> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FinPaymentPay::getId, id)
                .set(FinPaymentPay::getStatus, Constants.ORDER_STATUS_AUDITED)
                .set(FinPaymentPay::getAuditBy, auditBy)
                .set(FinPaymentPay::getAuditTime, LocalDateTime.now());
        finPaymentPayMapper.update(null, updateWrapper);

        return R.ok();
    }

    /**
     * 作废付款单
     * 仅草稿状态的付款单可作废，已审核的付款单不可作废（需通过红冲方式处理）
     *
     * @param id 付款单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    @Override
    public R<Void> voidOrder(Long id) {
        FinPaymentPay pay = finPaymentPayMapper.selectById(id);
        if (pay == null) {
            return R.fail("付款单不存在");
        }
        if (Constants.ORDER_STATUS_VOID.equals(pay.getStatus())) {
            return R.fail("付款单已作废，不可重复操作");
        }
        if (Constants.ORDER_STATUS_AUDITED.equals(pay.getStatus())) {
            return R.fail("已审核的付款单不可作废，请通过红冲方式处理");
        }

        // 更新付款单状态为已作废
        LambdaUpdateWrapper<FinPaymentPay> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FinPaymentPay::getId, id)
                .set(FinPaymentPay::getStatus, Constants.ORDER_STATUS_VOID);
        finPaymentPayMapper.update(null, updateWrapper);

        return R.ok();
    }
}
