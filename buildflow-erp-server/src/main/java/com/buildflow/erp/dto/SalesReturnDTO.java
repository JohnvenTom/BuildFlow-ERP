package com.buildflow.erp.dto;

import com.buildflow.erp.entity.SalesReturn;
import com.buildflow.erp.entity.SalesReturnItem;
import lombok.Data;

import java.util.List;

/**
 * 销售退货单请求DTO
 * 封装退货单主表对象和明细列表，用于新增销售退货单接口的请求参数接收。
 */
@Data
public class SalesReturnDTO {

    /** 退货单主表对象 */
    private SalesReturn returnOrder;

    /** 退货单明细列表 */
    private List<SalesReturnItem> items;
}
