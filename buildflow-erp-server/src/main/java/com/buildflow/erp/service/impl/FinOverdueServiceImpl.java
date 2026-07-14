package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.CrmCustomer;
import com.buildflow.erp.entity.FinReceivable;
import com.buildflow.erp.mapper.CrmCustomerMapper;
import com.buildflow.erp.mapper.FinReceivableMapper;
import com.buildflow.erp.service.FinOverdueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 逾期应收服务实现类
 * 实现逾期应收账款查询的核心业务逻辑，包括查询所有逾期应收明细、按客户汇总逾期总金额等操作。
 * 逾期判断条件：状态非已结清、到期日早于今天、剩余金额大于0。
 */
@Service
public class FinOverdueServiceImpl implements FinOverdueService {

    @Autowired
    private FinReceivableMapper finReceivableMapper;

    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    /**
     * 查询所有逾期应收明细
     * 查询所有未结清且到期日早于今天的应收台账，关联客户名称，计算逾期天数
     *
     * @return 逾期明细列表，每条包含id、customerId、customerName、remainAmount、dueDate、overdueDays
     */
    @Override
    public R<List<Map<String, Object>>> overdueList() {
        LocalDate today = LocalDate.now();

        // 查询所有未结清且到期日早于今天、剩余金额大于0的应收记录
        LambdaQueryWrapper<FinReceivable> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(FinReceivable::getStatus, Constants.FIN_STATUS_SETTLED)
                .lt(FinReceivable::getDueDate, today)
                .gt(FinReceivable::getRemainAmount, BigDecimal.ZERO);
        List<FinReceivable> overdueReceivables = finReceivableMapper.selectList(wrapper);

        // 批量查询客户名称
        List<Long> customerIds = overdueReceivables.stream()
                .map(FinReceivable::getCustomerId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> customerNameMap = new HashMap<>();
        if (!customerIds.isEmpty()) {
            LambdaQueryWrapper<CrmCustomer> customerWrapper = new LambdaQueryWrapper<>();
            customerWrapper.in(CrmCustomer::getId, customerIds);
            List<CrmCustomer> customers = crmCustomerMapper.selectList(customerWrapper);
            customerNameMap = customers.stream()
                    .collect(Collectors.toMap(CrmCustomer::getId, CrmCustomer::getName, (a, b) -> a));
        }

        // 构建逾期明细列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (FinReceivable r : overdueReceivables) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("customerId", r.getCustomerId());
            map.put("customerName", customerNameMap.getOrDefault(r.getCustomerId(), "未知客户"));
            map.put("remainAmount", r.getRemainAmount());
            map.put("dueDate", r.getDueDate());
            map.put("overdueDays", ChronoUnit.DAYS.between(r.getDueDate(), today));
            result.add(map);
        }
        return R.ok(result);
    }

    /**
     * 按客户汇总逾期总金额
     * 按客户ID分组汇总所有逾期应收的剩余金额
     *
     * @return 汇总列表，每条包含customerId、customerName、overdueAmount
     */
    @Override
    public R<List<Map<String, Object>>> overdueCustomerSummary() {
        LocalDate today = LocalDate.now();

        // 查询所有逾期应收记录
        LambdaQueryWrapper<FinReceivable> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(FinReceivable::getStatus, Constants.FIN_STATUS_SETTLED)
                .lt(FinReceivable::getDueDate, today)
                .gt(FinReceivable::getRemainAmount, BigDecimal.ZERO);
        List<FinReceivable> overdueReceivables = finReceivableMapper.selectList(wrapper);

        // 按客户ID分组汇总逾期金额
        Map<Long, BigDecimal> groupByCustomer = new HashMap<>();
        for (FinReceivable r : overdueReceivables) {
            BigDecimal amount = groupByCustomer.getOrDefault(r.getCustomerId(), BigDecimal.ZERO);
            groupByCustomer.put(r.getCustomerId(), amount.add(r.getRemainAmount()));
        }

        // 批量查询客户名称
        List<Long> customerIds = new ArrayList<>(groupByCustomer.keySet());
        Map<Long, String> customerNameMap = new HashMap<>();
        if (!customerIds.isEmpty()) {
            LambdaQueryWrapper<CrmCustomer> customerWrapper = new LambdaQueryWrapper<>();
            customerWrapper.in(CrmCustomer::getId, customerIds);
            List<CrmCustomer> customers = crmCustomerMapper.selectList(customerWrapper);
            customerNameMap = customers.stream()
                    .collect(Collectors.toMap(CrmCustomer::getId, CrmCustomer::getName, (a, b) -> a));
        }

        // 构建汇总列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : groupByCustomer.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("customerId", entry.getKey());
            map.put("customerName", customerNameMap.getOrDefault(entry.getKey(), "未知客户"));
            map.put("overdueAmount", entry.getValue());
            result.add(map);
        }
        return R.ok(result);
    }
}
