package com.buildflow.erp.dto;

import com.buildflow.erp.entity.FinPaymentReceive;
import com.buildflow.erp.entity.FinPaymentReceiveItem;
import lombok.Data;

import java.util.List;

/**
 * 回款单请求DTO
 * 封装回款单主表对象和核销明细列表，用于新增回款单接口的请求参数接收。
 */
@Data
public class FinPaymentReceiveDTO {

    /** 回款单主表对象 */
    private FinPaymentReceive receive;

    /** 回款核销明细列表 */
    private List<FinPaymentReceiveItem> items;
}
