package com.buildflow.erp.service;

import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SysMenu;

import java.util.List;

/**
 * 菜单服务接口
 * 定义菜单管理的核心业务方法，包括菜单树查询、角色菜单查询、菜单分配等操作。
 */
public interface SysMenuService {

    /**
     * 查询全部菜单树
     * 返回所有菜单并按parentId组装为树形结构，用于菜单管理页面展示
     *
     * @return 树形结构的菜单列表，顶级菜单的parentId为0
     */
    R<List<SysMenu>> tree();

    /**
     * 根据角色ID获取菜单列表
     * 查询指定角色所关联的所有菜单，用于角色菜单分配回显
     *
     * @param roleId 角色ID，关联sys_role.id
     * @return 该角色关联的菜单列表
     */
    R<List<SysMenu>> getByRoleId(Long roleId);

    /**
     * 分配角色菜单
     * 先删除该角色原有的菜单关联，再批量插入新的关联记录
     *
     * @param roleId  角色ID，关联sys_role.id
     * @param menuIds 菜单ID列表，关联sys_menu.id
     * @return 操作结果，成功返回R.ok()
     */
    R<Void> assignMenus(Long roleId, List<Long> menuIds);
}
