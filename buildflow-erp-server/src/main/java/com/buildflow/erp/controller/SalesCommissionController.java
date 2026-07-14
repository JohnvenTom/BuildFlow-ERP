package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.R;
import com.buildflow.erp.service.SalesCommissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 销售提成管理控制器
 * 提供销售提成汇总报表查询接口
 */
@RestController
@RequestMapping("/sales/commission")
public class SalesCommissionController {

    @Autowired
    private SalesCommissionService salesCommissionService;

    /**
     * 销售提成汇总报表
     * 按时间段汇总每个业务员全部已审核销售订单的提成金额
     *
     * @param startTime 查询开始时间（格式：yyyy-MM-dd HH:mm:ss），可为空
     * @param endTime   查询结束时间（格式：yyyy-MM-dd HH:mm:ss），可为空
     * @return 业务员提成汇总列表，每个元素为Map，包含salespersonId、salespersonName、orderCount、totalCommission字段
     */
    @PreAuthorize("@ps.hasPermission('sales:commission:list')")
    @GetMapping("/report")
    public R<List<Map<String, Object>>> report(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return salesCommissionService.commissionReport(startTime, endTime);
    }
}
