package com.buildflow.erp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统菜单实体
 * 对应数据库表 sys_menu，映射系统菜单及权限标识。
 * 通过 parentId 构建菜单树形结构，menuType 区分目录、菜单、按钮，
 * permission 字段用于接口级权限控制。
 *
 * @TableName sys_menu
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父级菜单ID，顶级菜单为 0 */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 前端路由地址 */
    private String path;

    /** 菜单图标 */
    private String icon;

    /** 排序号，数值越小越靠前 */
    private Integer sort;

    /** 权限标识，用于接口级鉴权（如 user:add） */
    private String permission;

    /** 菜单类型（0-目录 1-菜单 2-按钮） */
    private Integer menuType;

    /** 逻辑删除标识（0-未删除 1-已删除） */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间，新增时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，新增和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 子菜单列表（非数据库字段，用于构建树形结构） */
    @TableField(exist = false)
    private List<SysMenu> children;
}
