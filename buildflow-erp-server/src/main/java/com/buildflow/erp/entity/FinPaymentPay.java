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
 * 供应商付款单主表实体
 * 对应数据库表 fin_payment_pay，映射供应商付款单主表信息，包括付款单号、供应商、金额、付款方式、审核状态等。
 * 付款单审核后核销对应应付台账。
 *
 * @TableName fin_payment_pay
 */
@Data
@TableName("fin_payment_pay")
public class FinPaymentPay {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 付款单号，格式 PP+日期+流水号 */
    private String payNo;

    /** 供应商ID，关联 crm_supplier.id */
    private Long supplierId;

    /** 付款总金额 */
    private BigDecimal amount;

    /** 付款方式（如：现金、银行转账、支票等） */
    private String payMethod;

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

    /** 供应商名称（关联查询，非数据库字段） */
    @TableField(exist = false)
    private String supplierName;
}
