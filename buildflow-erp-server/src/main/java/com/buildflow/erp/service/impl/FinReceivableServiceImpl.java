package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.CrmCustomer;
import com.buildflow.erp.entity.FinReceivable;
import com.buildflow.erp.mapper.CrmCustomerMapper;
import com.buildflow.erp.mapper.FinReceivableMapper;
import com.buildflow.erp.service.FinReceivableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户应收台账服务实现类
 * 实现应收账款管理的核心业务逻辑，包括分页查询、自动生成应收记录、按客户汇总应收数据等操作。
 */
@Service
public class FinReceivableServiceImpl implements FinReceivableService {

    @Autowired
    private FinReceivableMapper finReceivableMapper;

    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    /**
     * 按客户分页查询应收台账
     * 支持按客户ID和状态过滤，按创建时间倒序排列
     *
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param customerId 客户ID，可为null表示不过滤
     * @param status     状态（0-正常 1-逾期 2-已结清），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页应收台账列表
     */
    @Override
    public R<PageResult<FinReceivable>> page(Integer pageNum, Integer pageSize, Long customerId, String status) {
        Page<FinReceivable> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FinReceivable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(customerId != null, FinReceivable::getCustomerId, customerId)
                .eq(StringUtils.hasText(status), FinReceivable::getStatus, status)
                .orderByDesc(FinReceivable::getCreateTime);
        Page<FinReceivable> result = finReceivableMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    /**
     * 出库审核后自动生成应收记录
     * 根据客户ID、来源单据类型和ID、应收金额、到期日创建应收台账，
     * 初始已回款金额为0，剩余金额等于应收金额，状态为正常。
     *
     * @param customerId 客户ID
     * @param sourceType 来源单据类型（如：DELIVERY-出库单）
     * @param sourceId   来源单据ID
     * @param amount     应收金额
     * @param dueDate    到期日
     */
    @Override
    public void addReceivable(Long customerId, String sourceType, Long sourceId, BigDecimal amount, LocalDate dueDate) {
        FinReceivable receivable = new FinReceivable();
        receivable.setCustomerId(customerId);
        receivable.setSourceType(sourceType);
        receivable.setSourceId(sourceId);
        receivable.setAmount(amount);
        receivable.setPaidAmount(BigDecimal.ZERO);
        receivable.setRemainAmount(amount);
        receivable.setDueDate(dueDate);
        receivable.setStatus(Constants.FIN_STATUS_NORMAL);
        finReceivableMapper.insert(receivable);
    }

    /**
     * 按客户汇总应收总金额、已回款、剩余、逾期金额
     * 汇总所有未删除客户的应收数据，逾期金额按状态为逾期且剩余大于0的记录统计
     *
     * @return 汇总列表，每条包含customerId、customerName、totalAmount、paidAmount、remainAmount、overdueAmount
     */
    @Override
    public R<List<Map<String, Object>>> summaryByCustomer() {
        // 查询全部应收记录
        LambdaQueryWrapper<FinReceivable> wrapper = new LambdaQueryWrapper<>();
        List<FinReceivable> allReceivables = finReceivableMapper.selectList(wrapper);

        // 按客户ID分组
        Map<Long, List<FinReceivable>> groupByCustomer = allReceivables.stream()
                .collect(Collectors.groupingBy(FinReceivable::getCustomerId));

        // 批量查询客户名称
        LambdaQueryWrapper<CrmCustomer> customerWrapper = new LambdaQueryWrapper<>();
        List<CrmCustomer> customers = crmCustomerMapper.selectList(customerWrapper);
        Map<Long, String> customerNameMap = customers.stream()
                .collect(Collectors.toMap(CrmCustomer::getId, CrmCustomer::getName, (a, b) -> a));

        // 按客户汇总
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<FinReceivable>> entry : groupByCustomer.entrySet()) {
            Long customerId = entry.getKey();
            List<FinReceivable> receivables = entry.getValue();

            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal paidAmount = BigDecimal.ZERO;
            BigDecimal remainAmount = BigDecimal.ZERO;
            BigDecimal overdueAmount = BigDecimal.ZERO;

            for (FinReceivable r : receivables) {
                totalAmount = totalAmount.add(r.getAmount() == null ? BigDecimal.ZERO : r.getAmount());
                paidAmount = paidAmount.add(r.getPaidAmount() == null ? BigDecimal.ZERO : r.getPaidAmount());
                remainAmount = remainAmount.add(r.getRemainAmount() == null ? BigDecimal.ZERO : r.getRemainAmount());
                // 逾期金额：状态为逾期且剩余大于0
                if (Constants.FIN_STATUS_OVERDUE.equals(r.getStatus())
                        && r.getRemainAmount() != null
                        && r.getRemainAmount().compareTo(BigDecimal.ZERO) > 0) {
                    overdueAmount = overdueAmount.add(r.getRemainAmount());
                }
            }

            Map<String, Object> map = new HashMap<>();
            map.put("customerId", customerId);
            map.put("customerName", customerNameMap.getOrDefault(customerId, "未知客户"));
            map.put("totalAmount", totalAmount);
            map.put("paidAmount", paidAmount);
            map.put("remainAmount", remainAmount);
            map.put("overdueAmount", overdueAmount);
            result.add(map);
        }
        return R.ok(result);
    }
}
