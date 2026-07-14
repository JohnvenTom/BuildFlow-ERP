package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 调拨单明细实体
 * 对应数据库表 wms_stock_transfer_item，映射调拨单中每个商品的调拨数量。
 * 一个调拨单可包含多条明细记录，审核时按明细逐条扣减调出仓库存并增加调入仓库存。
 *
 * @TableName wms_stock_transfer_item
 */
@Data
@TableName("wms_stock_transfer_item")
public class WmsStockTransferItem {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 调拨单ID，关联 wms_stock_transfer.id */
    private Long transferId;

    /** 商品ID，关联 wms_product.id */
    private Long productId;

    /** 调拨数量 */
    private Integer quantity;
}
