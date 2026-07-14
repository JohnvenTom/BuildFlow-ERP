package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调拨单主表实体
 * 对应数据库表 wms_stock_transfer，映射调拨单基本信息，包括调出仓库、调入仓库、状态、审核信息等。
 * 用于仓库间库存调拨流转业务，审核后自动扣减调出仓库存并增加调入仓库存。
 *
 * @TableName wms_stock_transfer
 */
@Data
@TableName("wms_stock_transfer")
public class WmsStockTransfer {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 调拨单号，自动生成，格式如 ST202607140001 */
    private String transferNo;

    /** 调出仓库ID，关联 wms_warehouse.id */
    private Long fromWarehouseId;

    /** 调入仓库ID，关联 wms_warehouse.id */
    private Long toWarehouseId;

    /** 单据状态（0-草稿 1-已审核 2-已作废） */
    private String status;

    /** 备注 */
    private String remark;

    /** 创建人ID，关联 sys_user.id */
    private Long createBy;

    /** 审核人ID，关联 sys_user.id */
    private Long auditBy;

    /** 审核时间 */
    private LocalDateTime auditTime;

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
