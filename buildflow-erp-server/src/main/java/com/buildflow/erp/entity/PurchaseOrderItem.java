package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购订单明细实体
 * 对应数据库表 purchase_order_item，映射采购订单中每个商品的色号、单价、数量和金额。
 * 一个采购订单可包含多条明细记录，金额 = 单价 × 数量，新增时自动计算。
 *
 * @TableName purchase_order_item
 */
@Data
@TableName("purchase_order_item")
public class PurchaseOrderItem {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 采购订单ID，关联 purchase_order.id */
    private Long orderId;

    /** 商品ID，关联 wms_product.id */
    private Long productId;

    /** 色号 */
    private String colorNo;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 采购数量 */
    private Integer quantity;

    /** 金额（unitPrice × quantity，新增时自动计算） */
    private BigDecimal amount;
}
