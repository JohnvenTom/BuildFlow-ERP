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
 * 采购入库单主表实体
 * 对应数据库表 purchase_stock_in，映射采购入库单基本信息，包括关联采购订单、供应商、入库仓库、金额、状态等。
 * 审核入库单时通过事务调用WmsInventoryService.increaseStock增加库存，并生成供应商应付台账。
 *
 * @TableName purchase_stock_in
 */
@Data
@TableName("purchase_stock_in")
public class PurchaseStockIn {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 入库单号，自动生成，格式如 SI202607140001 */
    private String stockInNo;

    /** 关联采购订单ID，关联 purchase_order.id */
    private Long orderId;

    /** 供应商ID，关联 crm_supplier.id */
    private Long supplierId;

    /** 入库仓库ID，关联 wms_warehouse.id */
    private Long warehouseId;

    /** 入库总金额 */
    private BigDecimal totalAmount;

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
