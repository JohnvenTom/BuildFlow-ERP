package com.buildflow.erp.dto;

import com.buildflow.erp.entity.SalesOrder;
import com.buildflow.erp.entity.SalesOrderItem;
import lombok.Data;

import java.util.List;

/**
 * 销售订单请求DTO
 * 封装销售订单主表对象和明细列表，用于新增销售订单接口的请求参数接收。
 */
@Data
public class SalesOrderDTO {

    /** 销售订单主表对象 */
    private SalesOrder order;

    /** 销售订单明细列表 */
    private List<SalesOrderItem> items;
}
