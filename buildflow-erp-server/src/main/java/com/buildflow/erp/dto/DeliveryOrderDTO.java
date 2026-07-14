package com.buildflow.erp.dto;

import com.buildflow.erp.entity.DeliveryOrder;
import com.buildflow.erp.entity.DeliveryOrderItem;
import lombok.Data;

import java.util.List;

/**
 * 工地出库单请求DTO
 * 封装出库单主表对象和明细列表，用于新增出库单接口的请求参数接收。
 */
@Data
public class DeliveryOrderDTO {

    /** 出库单主表对象 */
    private DeliveryOrder delivery;

    /** 出库单明细列表 */
    private List<DeliveryOrderItem> items;
}
