package com.buildflow.erp.dto;

import com.buildflow.erp.entity.FinPaymentPay;
import com.buildflow.erp.entity.FinPaymentPayItem;
import lombok.Data;

import java.util.List;

/**
 * 付款单请求DTO
 * 封装付款单主表对象和核销明细列表，用于新增付款单接口的请求参数接收。
 */
@Data
public class FinPaymentPayDTO {

    /** 付款单主表对象 */
    private FinPaymentPay pay;

    /** 付款核销明细列表 */
    private List<FinPaymentPayItem> items;
}
