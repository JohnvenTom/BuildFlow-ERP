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
 * 工地分次出库单主表实体
 * 对应数据库表 delivery_order，映射工地分次出库单基本信息，包括关联销售订单、出库仓库、出库金额、状态等。
 * 一个销售订单可多次出库，审核出库单时通过事务扣减库存、累加订单明细已出库数量、增加客户欠款。
 *
 * @TableName delivery_order
 */
@Data
@TableName("delivery_order")
public class DeliveryOrder {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 出库单号，自动生成，格式如 DO202607140001 */
    private String deliveryNo;

    /** 关联销售订单ID，关联 sales_order.id */
    private Long orderId;

    /** 出库仓库ID，关联 wms_warehouse.id */
    private Long warehouseId;

    /** 出库总金额 */
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
