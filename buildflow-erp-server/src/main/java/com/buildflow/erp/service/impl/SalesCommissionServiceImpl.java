package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SalesOrder;
import com.buildflow.erp.entity.SysUser;
import com.buildflow.erp.mapper.SalesOrderMapper;
import com.buildflow.erp.mapper.SysUserMapper;
import com.buildflow.erp.service.SalesCommissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 销售提成服务实现类
 * 实现销售提成统计相关业务逻辑，提供按时间段汇总业务员提成金额的报表查询。
 * 统计范围限定为已审核状态的销售订单。
 */
@Service
public class SalesCommissionServiceImpl implements SalesCommissionService {

    /** 日期时间格式化器，用于解析前端传入的时间字符串 */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private SalesOrderMapper salesOrderMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 销售提成汇总报表
     * 按时间段汇总每个业务员全部已审核销售订单的提成金额，关联sys_user获取业务员姓名
     *
     * @param startTime 查询开始时间（格式：yyyy-MM-dd HH:mm:ss），可为null表示不限制起始时间
     * @param endTime   查询结束时间（格式：yyyy-MM-dd HH:mm:ss），可为null表示不限制截止时间
     * @return 业务员提成汇总列表，每个元素为Map，包含salespersonId、salespersonName、orderCount、totalCommission字段
     */
    @Override
    public R<List<Map<String, Object>>> commissionReport(String startTime, String endTime) {
        // 构建查询条件：已审核状态 + 创建时间区间
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesOrder::getStatus, Constants.ORDER_STATUS_AUDITED);
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(SalesOrder::getCreateTime, LocalDateTime.parse(startTime, FORMATTER));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(SalesOrder::getCreateTime, LocalDateTime.parse(endTime, FORMATTER));
        }
        List<SalesOrder> orders = salesOrderMapper.selectList(wrapper);

        // 按业务员ID分组统计提成金额和订单数
        Map<Long, BigDecimal> commissionMap = new LinkedHashMap<>();
        Map<Long, Integer> orderCountMap = new HashMap<>();
        for (SalesOrder order : orders) {
            Long salespersonId = order.getSalespersonId();
            if (salespersonId == null) {
                continue;
            }
            BigDecimal commission = order.getCommissionAmount() == null ? BigDecimal.ZERO : order.getCommissionAmount();
            commissionMap.merge(salespersonId, commission, BigDecimal::add);
            orderCountMap.merge(salespersonId, 1, Integer::sum);
        }

        // 批量查询业务员姓名
        Map<Long, String> nameMap = new HashMap<>();
        if (!commissionMap.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(commissionMap.keySet());
            nameMap = users.stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));
        }

        // 组装报表结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : commissionMap.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("salespersonId", entry.getKey());
            row.put("salespersonName", nameMap.getOrDefault(entry.getKey(), "未知"));
            row.put("orderCount", orderCountMap.getOrDefault(entry.getKey(), 0));
            row.put("totalCommission", entry.getValue());
            result.add(row);
        }
        return R.ok(result);
    }
}
