package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.dto.FinPaymentReceiveDTO;
import com.buildflow.erp.entity.FinPaymentReceive;
import com.buildflow.erp.service.FinPaymentReceiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 客户回款单控制器
 * 提供回款单的分页查询、新增、审核、作废接口
 */
@RestController
@RequestMapping("/finance/payment-receive")
public class FinPaymentReceiveController {

    @Autowired
    private FinPaymentReceiveService finPaymentReceiveService;

    /**
     * 分页查询回款单列表
     * 支持按回款单号模糊搜索、客户ID、状态过滤
     *
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param receiveNo  回款单号（模糊查询），可为空
     * @param customerId 客户ID，可为空
     * @param status     单据状态（0-草稿 1-已审核 2-已作废），可为空
     * @return 分页结果，包含总记录数和当前页回款单列表
     */
    @GetMapping("/page")
    public R<PageResult<FinPaymentReceive>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String receiveNo,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status) {
        return finPaymentReceiveService.page(pageNum, pageSize, receiveNo, customerId, status);
    }

    /**
     * 新增回款单
     * 自动生成回款单号，校验核销明细金额，插入主表和明细表
     *
     * @param dto 回款单请求DTO，包含回款单主表和核销明细列表
     * @return 操作结果
     */
    @PostMapping
    public R<Void> add(@RequestBody FinPaymentReceiveDTO dto) {
        return finPaymentReceiveService.add(dto.getReceive(), dto.getItems());
    }

    /**
     * 审核回款单
     * 事务操作：核销应收台账、减少客户欠款、更新状态为已审核
     *
     * @param id      回款单ID
     * @param auditBy 审核人ID
     * @return 操作结果
     */
    @PutMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id, @RequestParam Long auditBy) {
        return finPaymentReceiveService.audit(id, auditBy);
    }

    /**
     * 作废回款单
     * 仅草稿状态的回款单可作废
     *
     * @param id 回款单ID
     * @return 操作结果
     */
    @PutMapping("/void/{id}")
    public R<Void> voidOrder(@PathVariable Long id) {
        return finPaymentReceiveService.voidOrder(id);
    }
}
