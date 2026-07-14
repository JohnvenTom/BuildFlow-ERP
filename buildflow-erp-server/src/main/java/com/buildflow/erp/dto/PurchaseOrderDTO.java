package com.buildflow.erp.dto;

import com.buildflow.erp.entity.PurchaseOrder;
import com.buildflow.erp.entity.PurchaseOrderItem;
import lombok.Data;

import java.util.List;

/**
 * 采购订单请求DTO
 * 封装采购订单主表对象和明细列表，用于新增采购订单接口的请求参数接收。
 */
@Data
public class PurchaseOrderDTO {

    /** 采购订单主表对象 */
    private PurchaseOrder order;

    /** 采购订单明细列表 */
    private List<PurchaseOrderItem> items;
}
