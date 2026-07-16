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
 * 客户跟进记录实体
 * 对应数据库表 crm_follow_record，映射客户跟进的类型、内容、下次拜访时间等字段。
 * 用于销售跟进管理、客户拜访计划、跟进历史查询等业务场景。
 *
 * @TableName crm_follow_record
 */
@Data
@TableName("crm_follow_record")
public class CrmFollowRecord {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户ID，关联 crm_customer.id */
    private Long customerId;

    /** 客户名称（非数据库字段，关联查询 crm_customer.name 填充） */
    @TableField(exist = false)
    private String customerName;

    /** 跟进类型（如：电话、拜访、微信等） */
    private String followType;

    /** 跟进内容 */
    private String content;

    /** 下次拜访时间 */
    private LocalDateTime nextVisitTime;

    /** 业务员ID，关联 sys_user.id */
    private Long salespersonId;

    /** 业务员名称（非数据库字段，关联查询 sys_user.realName 填充） */
    @TableField(exist = false)
    private String salespersonName;

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
