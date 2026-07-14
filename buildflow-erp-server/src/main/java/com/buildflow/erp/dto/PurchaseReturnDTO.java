package com.buildflow.erp.dto;

import com.buildflow.erp.entity.PurchaseReturn;
import com.buildflow.erp.entity.PurchaseReturnItem;
import lombok.Data;

import java.util.List;

/**
 * 采购退货单请求DTO
 * 封装退货单主表对象和明细列表，用于新增退货单接口的请求参数接收。
 */
@Data
public class PurchaseReturnDTO {

    /** 退货单主表对象 */
    private PurchaseReturn returnOrder;

    /** 退货单明细列表 */
    private List<PurchaseReturnItem> items;
}
