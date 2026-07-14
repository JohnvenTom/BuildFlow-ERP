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
 * 供应商档案实体
 * 对应数据库表 crm_supplier，映射供应商基本信息、主营类目、结算周期、应付额度等字段。
 * 用于供应商管理、采购对账、合作状态跟踪等业务场景。
 *
 * @TableName crm_supplier
 */
@Data
@TableName("crm_supplier")
public class CrmSupplier {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 供应商名称 */
    private String name;

    /** 主营类目 */
    private String mainCategory;

    /** 联系人 */
    private String contact;

    /** 联系电话 */
    private String phone;

    /** 结算周期（如：月结30天、月结60天等） */
    private String settlementCycle;

    /** 应付额度上限 */
    private BigDecimal payableLimit;

    /** 合作状态（0-正常 1-暂停 2-终止） */
    private Integer cooperationStatus;

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
