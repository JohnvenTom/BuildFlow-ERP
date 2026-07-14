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
import java.util.List;

/**
 * 销售订单主表实体
 * 对应数据库表 sales_order，映射销售订单基本信息，包括客户、业务员、销售金额、提成信息、状态等。
 * 用于销售业务管理，审核后可关联生成工地分次出库单。
 *
 * @TableName sales_order
 */
@Data
@TableName("sales_order")
public class SalesOrder {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 销售订单号，自动生成，格式如 SO202607140001 */
    private String orderNo;

    /** 客户ID，关联 crm_customer.id */
    private Long customerId;

    /** 业务员ID，关联 sys_user.id */
    private Long salespersonId;

    /** 销售总金额 */
    private BigDecimal totalAmount;

    /** 提成比例（0-1之间，如0.05表示5%） */
    private BigDecimal commissionRate;

    /** 提成金额（totalAmount × commissionRate，新增时自动计算） */
    private BigDecimal commissionAmount;

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

    /** 订单明细列表，非数据库字段，仅用于订单详情接口返回明细数据 */
    @TableField(exist = false)
    private List<SalesOrderItem> items;
}
