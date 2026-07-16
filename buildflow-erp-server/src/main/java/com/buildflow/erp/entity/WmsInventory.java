package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存明细实体
 * 对应数据库表 wms_inventory，映射商品在各仓库的批次库存数量。
 * 注意：此表无逻辑删除字段和创建时间字段，仅有更新时间。
 * 用于库存查询、出入库扣减增加、调拨流转、盘点修正等业务场景。
 *
 * @TableName wms_inventory
 */
@Data
@TableName("wms_inventory")
public class WmsInventory {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID，关联 wms_product.id */
    private Long productId;

    /** 仓库ID，关联 wms_warehouse.id */
    private Long warehouseId;

    /** 商品名称（非数据库字段，关联查询 wms_product.name 填充） */
    @TableField(exist = false)
    private String productName;

    /** 仓库名称（非数据库字段，关联查询 wms_warehouse.name 填充） */
    @TableField(exist = false)
    private String warehouseName;

    /** 批次号 */
    private String batchNo;

    /** 色号 */
    private String colorNo;

    /** 库存数量 */
    private Integer quantity;

    /** 更新时间，新增和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
