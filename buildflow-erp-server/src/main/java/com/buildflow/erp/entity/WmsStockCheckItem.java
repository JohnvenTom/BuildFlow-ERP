package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 盘点单明细实体
 * 对应数据库表 wms_stock_check_item，映射盘点单中每个商品的系统库存、实际库存和差异数量。
 * 差异数量 = 实际库存 - 系统库存，正数表示盘盈，负数表示盘亏。
 * 审核时根据差异值修正库存。
 *
 * @TableName wms_stock_check_item
 */
@Data
@TableName("wms_stock_check_item")
public class WmsStockCheckItem {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 盘点单ID，关联 wms_stock_check.id */
    private Long checkId;

    /** 商品ID，关联 wms_product.id */
    private Long productId;

    /** 系统库存数量（盘点时自动读取） */
    private Integer systemQty;

    /** 实际库存数量（人工盘点录入） */
    private Integer actualQty;

    /** 差异数量（actualQty - systemQty，正数盘盈，负数盘亏） */
    private Integer diffQty;
}
