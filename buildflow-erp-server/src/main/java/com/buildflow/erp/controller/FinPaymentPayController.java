package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.dto.FinPaymentPayDTO;
import com.buildflow.erp.entity.FinPaymentPay;
import com.buildflow.erp.service.FinPaymentPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商付款单控制器
 * 提供付款单的分页查询、新增、审核、作废接口
 */
@RestController
@RequestMapping("/finance/payment-pay")
public class FinPaymentPayController {

    @Autowired
    private FinPaymentPayService finPaymentPayService;

    /**
     * 分页查询付款单列表
     * 支持按付款单号模糊搜索、供应商ID、状态过滤
     *
     * @param pageNum     当前页码，从1开始
     * @param pageSize    每页记录数
     * @param payNo       付款单号（模糊查询），可为空
     * @param supplierId  供应商ID，可为空
     * @param status      单据状态（0-草稿 1-已审核 2-已作废），可为空
     * @return 分页结果，包含总记录数和当前页付款单列表
     */
    @PreAuthorize("@ps.hasPermission('fin:pay:list')")
    @GetMapping("/page")
    public R<PageResult<FinPaymentPay>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String payNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status) {
        return finPaymentPayService.page(pageNum, pageSize, payNo, supplierId, status);
    }

    /**
     * 新增付款单
     * 自动生成付款单号，校验核销明细金额，插入主表和明细表
     *
     * @param dto 付款单请求DTO，包含付款单主表和核销明细列表
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('fin:pay:add')")
    @PostMapping
    public R<Void> add(@RequestBody FinPaymentPayDTO dto) {
        return finPaymentPayService.add(dto.getPay(), dto.getItems());
    }

    /**
     * 审核付款单
     * 事务操作：核销应付台账、更新状态为已审核
     *
     * @param id      付款单ID
     * @param auditBy 审核人ID
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('fin:pay:audit')")
    @PutMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id, @RequestParam Long auditBy) {
        return finPaymentPayService.audit(id, auditBy);
    }

    /**
     * 作废付款单
     * 仅草稿状态的付款单可作废
     *
     * @param id 付款单ID
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('fin:pay:void')")
    @PutMapping("/void/{id}")
    public R<Void> voidOrder(@PathVariable Long id) {
        return finPaymentPayService.voidOrder(id);
    }
}
