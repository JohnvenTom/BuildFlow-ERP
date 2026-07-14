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
 * 系统用户实体
 * 对应数据库表 sys_user，映射系统用户基本信息、登录凭证、账号状态等字段。
 * 用于用户认证、授权、账号锁定及登录失败次数统计等业务场景。
 *
 * @TableName sys_user
 */
@Data
@TableName("sys_user")
public class SysUser {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名，唯一 */
    private String username;

    /** 登录密码（加密存储） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 联系电话 */
    private String phone;

    /** 角色ID，关联 sys_role.id */
    private Long roleId;

    /** 账号状态（0-正常 1-禁用） */
    private Integer status;

    /** 连续登录失败次数 */
    private Integer loginFailCount;

    /** 账号锁定截止时间，超过该时间自动解锁 */
    private LocalDateTime lockedUntil;

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
