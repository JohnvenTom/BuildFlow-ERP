package com.buildflow.erp.dto;

import com.buildflow.erp.entity.PurchaseStockIn;
import com.buildflow.erp.entity.PurchaseStockInItem;
import lombok.Data;

import java.util.List;

/**
 * 采购入库单请求DTO
 * 封装入库单主表对象和明细列表，用于新增入库单接口的请求参数接收。
 */
@Data
public class PurchaseStockInDTO {

    /** 入库单主表对象 */
    private PurchaseStockIn stockIn;

    /** 入库单明细列表 */
    private List<PurchaseStockInItem> items;
}
