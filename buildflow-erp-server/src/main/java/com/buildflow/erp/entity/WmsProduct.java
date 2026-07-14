package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品档案实体
 * 对应数据库表 wms_product，映射商品基本信息、规格、色号、价格、最低库存等字段。
 * 用于库存管理、采购销售定价、低库存预警等业务场景。
 *
 * @TableName wms_product
 */
@Data
@TableName("wms_product")
public class WmsProduct {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品分类 */
    private String category;

    /** 规格型号 */
    private String spec;

    /** 色号 */
    private String colorNo;

    /** 计量单位 */
    private String unit;

    /** 采购价格 */
    private BigDecimal purchasePrice;

    /** 销售价格 */
    private BigDecimal salePrice;

    /** 最低库存预警阈值 */
    private Integer minStock;

    /** 逻辑删除标识（0-未删除 1-已删除） */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间，新增时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，新增和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
