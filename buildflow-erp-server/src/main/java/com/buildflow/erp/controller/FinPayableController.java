package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.FinPayable;
import com.buildflow.erp.service.FinPayableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 供应商应付台账控制器
 * 提供应付台账的分页查询和按供应商汇总查询接口
 */
@RestController
@RequestMapping("/finance/payable")
public class FinPayableController {

    @Autowired
    private FinPayableService finPayableService;

    /**
     * 分页查询应付台账
     * 支持按供应商ID和状态过滤
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param supplierId  供应商ID，可为空
     * @param status      状态（0-正常 1-逾期 2-已结清），可为空
     * @return 分页结果，包含总记录数和当前页应付台账列表
     */
    @GetMapping("/page")
    public R<PageResult<FinPayable>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status) {
        return finPayableService.page(pageNum, pageSize, supplierId, status);
    }

    /**
     * 按供应商汇总应付数据
     * 汇总各供应商的应付总金额、已付款、剩余
     *
     * @return 汇总列表
     */
    @GetMapping("/summary")
    public R<List<Map<String, Object>>> summary() {
        return finPayableService.summaryBySupplier();
    }
}
