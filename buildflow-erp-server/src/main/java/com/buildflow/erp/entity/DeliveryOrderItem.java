package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 工地出库明细实体
 * 对应数据库表 delivery_order_item，映射出库单中每个商品的色号、数量和单价。
 * 一个出库单可包含多条明细记录，审核时按明细逐条调用decreaseStock扣减库存。
 *
 * @TableName delivery_order_item
 */
@Data
@TableName("delivery_order_item")
public class DeliveryOrderItem {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 出库单ID，关联 delivery_order.id */
    private Long deliveryId;

    /** 商品ID，关联 wms_product.id */
    private Long productId;

    /** 色号 */
    private String colorNo;

    /** 出库数量 */
    private Integer quantity;

    /** 单价 */
    private BigDecimal unitPrice;
}
