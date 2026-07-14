package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购入库单明细实体
 * 对应数据库表 purchase_stock_in_item，映射入库单中每个商品的批次号、色号、数量和单价。
 * 一个入库单可包含多条明细记录，审核时按明细逐条调用increaseStock增加库存。
 *
 * @TableName purchase_stock_in_item
 */
@Data
@TableName("purchase_stock_in_item")
public class PurchaseStockInItem {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 入库单ID，关联 purchase_stock_in.id */
    private Long stockInId;

    /** 商品ID，关联 wms_product.id */
    private Long productId;

    /** 色号 */
    private String colorNo;

    /** 批次号 */
    private String batchNo;

    /** 入库数量 */
    private Integer quantity;

    /** 单价 */
    private BigDecimal unitPrice;
}
