package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统操作日志实体
 * 对应数据库表 sys_operation_log，记录用户在系统中的关键操作行为。
 * 用于操作审计、问题排查及安全追溯，通常由 AOP 切面自动采集写入。
 *
 * @TableName sys_operation_log
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户ID，关联 sys_user.id */
    private Long userId;

    /** 操作用户名，冗余存储便于日志展示 */
    private String username;

    /** 操作所属模块 */
    private String module;

    /** 操作描述 */
    private String operation;

    /** 请求方法（类名#方法名） */
    private String method;

    /** 请求参数，JSON 格式字符串 */
    private String params;

    /** 请求IP地址 */
    private String ip;

    /** 操作时间，新增时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
