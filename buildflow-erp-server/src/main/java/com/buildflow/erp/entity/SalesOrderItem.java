package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售订单明细实体
 * 对应数据库表 sales_order_item，映射销售订单中每个商品的色号、单价、数量、折扣和金额。
 * 一个销售订单可包含多条明细记录，金额 = 单价 × 数量 × 折扣，新增时自动计算。
 * deliveredQty 字段记录该明细已出库数量，用于工地分次出库时的数量校验。
 *
 * @TableName sales_order_item
 */
@Data
@TableName("sales_order_item")
public class SalesOrderItem {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 销售订单ID，关联 sales_order.id */
    private Long orderId;

    /** 商品ID，关联 wms_product.id */
    private Long productId;

    /** 色号 */
    private String colorNo;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 销售数量 */
    private Integer quantity;

    /** 折扣率（0-1之间，如0.9表示九折，为null时按1处理） */
    private BigDecimal discount;

    /** 金额（unitPrice × quantity × discount，新增时自动计算） */
    private BigDecimal amount;

    /** 已出库数量，出库单审核时累加，用于校验分次出库不超量 */
    private Integer deliveredQty;
}
