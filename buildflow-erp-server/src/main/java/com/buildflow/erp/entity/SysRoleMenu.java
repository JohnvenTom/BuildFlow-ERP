package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色菜单关联实体
 * 对应数据库表 sys_role_menu，维护角色与菜单的多对多关系。
 * 通过 roleId 关联 sys_role.id，menuId 关联 sys_menu.id，
 * 实现按角色分配菜单及操作权限。
 *
 * @TableName sys_role_menu
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色ID，关联 sys_role.id */
    private Long roleId;

    /** 菜单ID，关联 sys_menu.id */
    private Long menuId;
}
