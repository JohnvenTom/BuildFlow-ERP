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
 * 客户档案实体
 * 对应数据库表 crm_customer，映射客户基本信息、联系方式、信用额度、当前欠款等字段。
 * 用于客户管理、信用控制、销售归属等业务场景。
 *
 * @TableName crm_customer
 */
@Data
@TableName("crm_customer")
public class CrmCustomer {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户名称 */
    private String name;

    /** 客户类型（如：企业、个人等） */
    private String type;

    /** 客户地址 */
    private String address;

    /** 联系人 */
    private String contact;

    /** 联系电话 */
    private String phone;

    /** 信用额度 */
    private BigDecimal creditLimit;

    /** 当前欠款金额 */
    private BigDecimal currentDebt;

    /** 业务员ID，关联 sys_user.id */
    private Long salespersonId;

    /** 客户状态（0-正常 1-作废） */
    private Integer status;

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
