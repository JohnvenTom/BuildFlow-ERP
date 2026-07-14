package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.service.CrmCreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 信用查询控制器
 * 提供客户信用与逾期信息查询接口
 */
@RestController
@RequestMapping("/crm/credit")
public class CrmCreditController {

    @Autowired
    private CrmCreditService crmCreditService;

    /**
     * 分页查询客户信用与逾期信息
     * 查询所有客户的当前应收总额、信用剩余额度、逾期金额、逾期天数
     *
     * @param pageNum       当前页码，从1开始
     * @param pageSize      每页记录数
     * @param customerName  客户名称（模糊查询），可为空
     * @param debtStatus    欠款状态（overdue-逾期/normal-正常），可为空
     * @return 分页结果，每条记录为客户信用信息Map
     */
    @GetMapping("/page")
    public R<PageResult<Map<String, Object>>> creditPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String debtStatus) {
        return crmCreditService.creditPage(pageNum, pageSize, customerName, debtStatus);
    }
}
