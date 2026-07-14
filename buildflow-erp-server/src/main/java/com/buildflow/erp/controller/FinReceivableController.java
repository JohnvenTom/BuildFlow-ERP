package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.FinReceivable;
import com.buildflow.erp.service.FinReceivableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 客户应收台账控制器
 * 提供应收台账的分页查询和按客户汇总查询接口
 */
@RestController
@RequestMapping("/finance/receivable")
public class FinReceivableController {

    @Autowired
    private FinReceivableService finReceivableService;

    /**
     * 分页查询应收台账
     * 支持按客户ID和状态过滤
     *
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param customerId 客户ID，可为空
     * @param status     状态（0-正常 1-逾期 2-已结清），可为空
     * @return 分页结果，包含总记录数和当前页应收台账列表
     */
    @PreAuthorize("@ps.hasPermission('fin:receivable:list')")
    @GetMapping("/page")
    public R<PageResult<FinReceivable>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status) {
        return finReceivableService.page(pageNum, pageSize, customerId, status);
    }

    /**
     * 按客户汇总应收数据
     * 汇总各客户的应收总金额、已回款、剩余、逾期金额
     *
     * @return 汇总列表
     */
    @PreAuthorize("@ps.hasPermission('fin:receivable:list')")
    @GetMapping("/summary")
    public R<List<Map<String, Object>>> summary() {
        return finReceivableService.summaryByCustomer();
    }
}
