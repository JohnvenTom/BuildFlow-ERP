package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.R;
import com.buildflow.erp.service.FinOverdueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 逾期应收控制器
 * 提供逾期应收明细查询和按客户汇总逾期金额查询接口
 */
@RestController
@RequestMapping("/finance/overdue")
public class FinOverdueController {

    @Autowired
    private FinOverdueService finOverdueService;

    /**
     * 查询所有逾期应收明细
     * 返回所有未结清且已到期的应收记录，包含客户名、逾期金额、逾期天数
     *
     * @return 逾期明细列表
     */
    @PreAuthorize("@ps.hasPermission('fin:overdue:list')")
    @GetMapping("/list")
    public R<List<Map<String, Object>>> list() {
        return finOverdueService.overdueList();
    }

    /**
     * 按客户汇总逾期总金额
     * 按客户分组汇总所有逾期应收的剩余金额
     *
     * @return 汇总列表
     */
    @PreAuthorize("@ps.hasPermission('fin:overdue:list')")
    @GetMapping("/summary")
    public R<List<Map<String, Object>>> summary() {
        return finOverdueService.overdueCustomerSummary();
    }
}
