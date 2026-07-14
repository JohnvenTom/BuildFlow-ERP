package com.buildflow.erp.dto;

import lombok.Data;

import java.util.List;

/**
 * 菜单视图对象
 * 用于前端菜单树渲染，包含菜单基础信息及子菜单列表，支持树形递归嵌套
 */
@Data
public class MenuVO {

    /** 菜单ID */
    private Long id;

    /** 父级菜单ID，顶级菜单为0 */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 前端路由地址 */
    private String path;

    /** 菜单图标 */
    private String icon;

    /** 排序号，数值越小越靠前 */
    private Integer sort;

    /** 子菜单列表，用于树形结构递归嵌套 */
    private List<MenuVO> children;
}
