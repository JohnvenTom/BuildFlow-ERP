package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.CrmCustomer;
import com.buildflow.erp.mapper.CrmCustomerMapper;
import com.buildflow.erp.service.CrmCreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 信用与逾期查询服务实现类
 * 实现客户信用额度、应收总额、逾期金额、逾期天数等查询逻辑。
 * 当前版本基于客户表数据计算信用信息，待集成应收款模块后将替换为真实应收数据。
 */
@Service
public class CrmCreditServiceImpl implements CrmCreditService {

    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    // TODO: 待集成财务模块后注入 FinReceivableMapper
    // @Autowired
    // private FinReceivableMapper finReceivableMapper;

    /**
     * 分页查询客户信用与逾期信息
     * 查询所有客户的当前应收总额（currentDebt）、信用剩余额度（creditLimit - currentDebt）、
     * 逾期金额、逾期天数。当前版本逾期数据基于客户表字段模拟，待应收款模块集成后替换。
     *
     * @param pageNum       当前页码，从1开始
     * @param pageSize      每页记录数
     * @param customerName  客户名称（模糊查询），可为null
     * @param debtStatus    欠款状态（overdue-逾期/normal-正常），可为null表示不过滤
     * @return 分页结果，每条记录为客户信用信息Map
     */
    @Override
    public R<PageResult<Map<String, Object>>> creditPage(Integer pageNum, Integer pageSize,
                                                         String customerName, String debtStatus) {
        // 先查询客户列表
        Page<CrmCustomer> customerPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CrmCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(customerName), CrmCustomer::getName, customerName)
                .orderByDesc(CrmCustomer::getCreateTime);
        Page<CrmCustomer> result = crmCustomerMapper.selectPage(customerPage, wrapper);

        // 构建信用信息列表
        List<Map<String, Object>> creditList = new ArrayList<>();
        for (CrmCustomer customer : result.getRecords()) {
            Map<String, Object> creditMap = buildCreditInfo(customer);
            // 欠款状态过滤
            if (StringUtils.hasText(debtStatus)) {
                boolean isOverdue = isOverdue(creditMap);
                if ("overdue".equals(debtStatus) && !isOverdue) {
                    continue;
                }
                if ("normal".equals(debtStatus) && isOverdue) {
                    continue;
                }
            }
            creditList.add(creditMap);
        }

        return R.ok(new PageResult<>(result.getTotal(), creditList));
    }

    /**
     * 构建单个客户的信用信息Map
     * 包含客户ID、客户名称、信用额度、当前欠款、信用剩余额度、逾期金额、逾期天数
     *
     * @param customer 客户实体对象
     * @return 信用信息Map，包含customerId、customerName、creditLimit、currentDebt、
     *         remainCredit、overdueAmount、overdueDays等字段
     */
    private Map<String, Object> buildCreditInfo(CrmCustomer customer) {
        Map<String, Object> map = new HashMap<>();
        map.put("customerId", customer.getId());
        map.put("customerName", customer.getName());

        BigDecimal creditLimit = customer.getCreditLimit() != null ? customer.getCreditLimit() : BigDecimal.ZERO;
        BigDecimal currentDebt = customer.getCurrentDebt() != null ? customer.getCurrentDebt() : BigDecimal.ZERO;
        BigDecimal remainCredit = creditLimit.subtract(currentDebt);

        map.put("creditLimit", creditLimit);
        map.put("currentDebt", currentDebt);
        map.put("remainCredit", remainCredit);

        // TODO: 待集成应收款模块后，从FinReceivable表汇总逾期金额和逾期天数
        // 当前版本基于欠款是否超过信用额度来模拟逾期判断
        BigDecimal overdueAmount = BigDecimal.ZERO;
        int overdueDays = 0;
        if (currentDebt.compareTo(creditLimit) > 0 && creditLimit.compareTo(BigDecimal.ZERO) > 0) {
            overdueAmount = currentDebt.subtract(creditLimit);
            overdueDays = 30; // 模拟逾期天数
        }
        map.put("overdueAmount", overdueAmount);
        map.put("overdueDays", overdueDays);

        return map;
    }

    /**
     * 判断客户是否逾期
     * 当前版本基于欠款是否超过信用额度判断，待应收款模块集成后替换为真实逾期逻辑
     *
     * @param creditMap 信用信息Map
     * @return true-逾期，false-正常
     */
    private boolean isOverdue(Map<String, Object> creditMap) {
        BigDecimal overdueAmount = (BigDecimal) creditMap.get("overdueAmount");
        return overdueAmount != null && overdueAmount.compareTo(BigDecimal.ZERO) > 0;
    }
}
