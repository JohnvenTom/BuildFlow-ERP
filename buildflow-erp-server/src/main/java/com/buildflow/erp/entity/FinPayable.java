package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商应付台账实体
 * 对应数据库表 fin_payable，映射供应商应付账款明细，包括应付金额、已付款金额、剩余金额、到期日等。
 * 由入库单审核后自动生成，用于财务对账、付款核销等业务场景。
 * 注意：本表无逻辑删除字段，记录一经生成不可删除。
 *
 * @TableName fin_payable
 */
@Data
@TableName("fin_payable")
public class FinPayable {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 供应商ID，关联 crm_supplier.id */
    private Long supplierId;

    /** 来源单据类型（如：STOCK_IN-入库单） */
    private String sourceType;

    /** 来源单据ID，关联对应业务单据主键 */
    private Long sourceId;

    /** 应付总金额 */
    private BigDecimal amount;

    /** 已付款金额 */
    private BigDecimal paidAmount;

    /** 剩余应付金额（amount - paidAmount） */
    private BigDecimal remainAmount;

    /** 到期日，用于逾期判断 */
    private LocalDate dueDate;

    /** 状态（0-正常 1-逾期 2-已结清） */
    private String status;

    /** 创建时间，新增时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，新增和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
