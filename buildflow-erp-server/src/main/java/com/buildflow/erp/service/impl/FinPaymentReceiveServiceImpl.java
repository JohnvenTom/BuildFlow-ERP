package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.common.utils.OrderNoGenerator;
import com.buildflow.erp.entity.CrmCustomer;
import com.buildflow.erp.entity.FinPaymentReceive;
import com.buildflow.erp.entity.FinPaymentReceiveItem;
import com.buildflow.erp.entity.FinReceivable;
import com.buildflow.erp.mapper.CrmCustomerMapper;
import com.buildflow.erp.mapper.FinPaymentReceiveItemMapper;
import com.buildflow.erp.mapper.FinPaymentReceiveMapper;
import com.buildflow.erp.mapper.FinReceivableMapper;
import com.buildflow.erp.service.FinPaymentReceiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 客户回款单服务实现类
 * 实现回款单管理的核心业务逻辑，包括分页查询、新增回款单（含核销明细）、审核回款单、作废回款单等操作。
 * 审核时通过事务核销应收台账并减少客户当前欠款，保证数据一致性。
 */
@Service
public class FinPaymentReceiveServiceImpl implements FinPaymentReceiveService {

    @Autowired
    private FinPaymentReceiveMapper finPaymentReceiveMapper;

    @Autowired
    private FinPaymentReceiveItemMapper finPaymentReceiveItemMapper;

    @Autowired
    private FinReceivableMapper finReceivableMapper;

    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    /**
     * 分页查询回款单列表
     * 支持按回款单号模糊搜索、客户ID、状态过滤，按创建时间倒序排列
     *
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param receiveNo  回款单号（模糊查询），可为null
     * @param customerId 客户ID，可为null表示不过滤
     * @param status     单据状态（0-草稿 1-已审核 2-已作废），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页回款单列表
     */
    @Override
    public R<PageResult<FinPaymentReceive>> page(Integer pageNum, Integer pageSize, String receiveNo, Long customerId, String status) {
        Page<FinPaymentReceive> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FinPaymentReceive> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(receiveNo), FinPaymentReceive::getReceiveNo, receiveNo)
                .eq(customerId != null, FinPaymentReceive::getCustomerId, customerId)
                .eq(StringUtils.hasText(status), FinPaymentReceive::getStatus, status)
                .orderByDesc(FinPaymentReceive::getCreateTime);
        Page<FinPaymentReceive> result = finPaymentReceiveMapper.selectPage(page, wrapper);
        List<FinPaymentReceive> records = result.getRecords();
        // 批量查询客户名称并填充到结果中
        List<Long> customerIds = records.stream()
                .map(FinPaymentReceive::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!customerIds.isEmpty()) {
            List<CrmCustomer> customers = crmCustomerMapper.selectBatchIds(customerIds);
            Map<Long, String> customerMap = customers.stream()
                    .collect(Collectors.toMap(CrmCustomer::getId, CrmCustomer::getName));
            records.forEach(e -> e.setCustomerName(customerMap.get(e.getCustomerId())));
        }
        return R.ok(new PageResult<>(result.getTotal(), records));
    }

    /**
     * 新增回款单及核销明细
     * 自动生成回款单号（RC+日期+流水号），设置初始状态为草稿，
     * 校验核销明细总金额不超过回款总金额，插入主表和明细表
     *
     * @param receive 回款单主表对象，需包含customerId、amount、payMethod等字段
     * @param items   核销明细列表，每条需包含receivableId、amount
     * @return 操作结果，核销明细总金额超过回款金额时返回R.fail()
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> add(FinPaymentReceive receive, List<FinPaymentReceiveItem> items) {
        // 生成回款单号并设置初始状态
        receive.setReceiveNo(OrderNoGenerator.generate(OrderNoGenerator.PAYMENT_RECEIVE));
        receive.setStatus(Constants.ORDER_STATUS_DRAFT);

        // 校验核销明细总金额不超过回款总金额
        if (items != null && !items.isEmpty()) {
            BigDecimal totalItemAmount = BigDecimal.ZERO;
            for (FinPaymentReceiveItem item : items) {
                totalItemAmount = totalItemAmount.add(item.getAmount());
            }
            if (totalItemAmount.compareTo(receive.getAmount()) > 0) {
                return R.fail("核销明细总金额超过回款总金额");
            }
        }

        // 插入回款单主表
        finPaymentReceiveMapper.insert(receive);

        // 插入核销明细
        if (items != null && !items.isEmpty()) {
            for (FinPaymentReceiveItem item : items) {
                item.setReceiveId(receive.getId());
                finPaymentReceiveItemMapper.insert(item);
            }
        }
        return R.ok();
    }

    /**
     * 审核回款单
     * 事务操作：校验单据状态为草稿，逐条明细核销应收台账（增加paidAmount、减少remainAmount），
     * 若剩余金额为0则更新状态为已结清，减少客户当前欠款，更新回款单状态为已审核。
     *
     * @param id      回款单ID
     * @param auditBy 审核人ID
     * @return 操作结果，单据不存在或状态非草稿时返回R.fail()
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> audit(Long id, Long auditBy) {
        FinPaymentReceive receive = finPaymentReceiveMapper.selectById(id);
        if (receive == null) {
            return R.fail("回款单不存在");
        }
        if (!Constants.ORDER_STATUS_DRAFT.equals(receive.getStatus())) {
            return R.fail("回款单状态不允许审核");
        }

        // 查询核销明细
        LambdaQueryWrapper<FinPaymentReceiveItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(FinPaymentReceiveItem::getReceiveId, id);
        List<FinPaymentReceiveItem> items = finPaymentReceiveItemMapper.selectList(itemWrapper);

        // 逐条明细核销应收台账
        for (FinPaymentReceiveItem item : items) {
            FinReceivable receivable = finReceivableMapper.selectById(item.getReceivableId());
            if (receivable == null) {
                return R.fail("应收台账记录不存在，ID：" + item.getReceivableId());
            }
            // 校验核销金额不超过剩余应收金额
            if (item.getAmount().compareTo(receivable.getRemainAmount()) > 0) {
                return R.fail("核销金额超过应收剩余金额，应收台账ID：" + item.getReceivableId());
            }
            // 更新应收台账已回款和剩余金额
            BigDecimal newPaidAmount = receivable.getPaidAmount().add(item.getAmount());
            BigDecimal newRemainAmount = receivable.getRemainAmount().subtract(item.getAmount());
            LambdaUpdateWrapper<FinReceivable> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(FinReceivable::getId, receivable.getId())
                    .set(FinReceivable::getPaidAmount, newPaidAmount)
                    .set(FinReceivable::getRemainAmount, newRemainAmount);
            // 剩余金额为0时更新状态为已结清
            if (newRemainAmount.compareTo(BigDecimal.ZERO) == 0) {
                updateWrapper.set(FinReceivable::getStatus, Constants.FIN_STATUS_SETTLED);
            }
            finReceivableMapper.update(null, updateWrapper);
        }

        // 减少客户当前欠款
        CrmCustomer customer = crmCustomerMapper.selectById(receive.getCustomerId());
        if (customer != null) {
            BigDecimal currentDebt = customer.getCurrentDebt() == null ? BigDecimal.ZERO : customer.getCurrentDebt();
            BigDecimal newDebt = currentDebt.subtract(receive.getAmount());
            if (newDebt.compareTo(BigDecimal.ZERO) < 0) {
                newDebt = BigDecimal.ZERO;
            }
            LambdaUpdateWrapper<CrmCustomer> customerWrapper = new LambdaUpdateWrapper<>();
            customerWrapper.eq(CrmCustomer::getId, customer.getId())
                    .set(CrmCustomer::getCurrentDebt, newDebt);
            crmCustomerMapper.update(null, customerWrapper);
        }

        // 更新回款单状态为已审核
        LambdaUpdateWrapper<FinPaymentReceive> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FinPaymentReceive::getId, id)
                .set(FinPaymentReceive::getStatus, Constants.ORDER_STATUS_AUDITED)
                .set(FinPaymentReceive::getAuditBy, auditBy)
                .set(FinPaymentReceive::getAuditTime, LocalDateTime.now());
        finPaymentReceiveMapper.update(null, updateWrapper);

        return R.ok();
    }

    /**
     * 作废回款单
     * 仅草稿状态的回款单可作废，已审核的回款单不可作废（需通过红冲方式处理）
     *
     * @param id 回款单ID
     * @return 操作结果，单据不存在或已作废时返回R.fail()
     */
    @Override
    public R<Void> voidOrder(Long id) {
        FinPaymentReceive receive = finPaymentReceiveMapper.selectById(id);
        if (receive == null) {
            return R.fail("回款单不存在");
        }
        if (Constants.ORDER_STATUS_VOID.equals(receive.getStatus())) {
            return R.fail("回款单已作废，不可重复操作");
        }
        if (Constants.ORDER_STATUS_AUDITED.equals(receive.getStatus())) {
            return R.fail("已审核的回款单不可作废，请通过红冲方式处理");
        }

        // 更新回款单状态为已作废
        LambdaUpdateWrapper<FinPaymentReceive> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FinPaymentReceive::getId, id)
                .set(FinPaymentReceive::getStatus, Constants.ORDER_STATUS_VOID);
        finPaymentReceiveMapper.update(null, updateWrapper);

        return R.ok();
    }
}
