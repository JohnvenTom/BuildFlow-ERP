package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SysMenu;
import com.buildflow.erp.entity.SysRole;
import com.buildflow.erp.service.SysMenuService;
import com.buildflow.erp.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * 提供角色的增删改查及角色菜单分配接口
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 查询全部角色列表
     * 返回系统中所有未删除的角色，通常用于角色下拉选择
     *
     * @return 角色列表
     */
    @PreAuthorize("@ps.hasPermission('sys:role:list')")
    @GetMapping("/list")
    public R<List<SysRole>> list() {
        return sysRoleService.list();
    }

    /**
     * 新增角色
     * 新增前会校验角色编码是否已存在
     *
     * @param role 角色实体对象，需包含roleName、roleCode字段
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('sys:role:add')")
    @PostMapping
    public R<Void> add(@RequestBody SysRole role) {
        return sysRoleService.add(role);
    }

    /**
     * 编辑角色信息
     * 不允许修改角色编码
     *
     * @param role 角色实体对象，id字段必填
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('sys:role:edit')")
    @PutMapping
    public R<Void> update(@RequestBody SysRole role) {
        return sysRoleService.update(role);
    }

    /**
     * 删除角色
     * 删除前会检查角色是否已分配给用户
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('sys:role:delete')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        return sysRoleService.delete(id);
    }

    /**
     * 获取角色已分配的菜单列表
     * 用于角色菜单分配时的回显
     *
     * @param roleId 角色ID
     * @return 该角色关联的菜单列表
     */
    @PreAuthorize("@ps.hasPermission('sys:role:list')")
    @GetMapping("/{roleId}/menus")
    public R<List<SysMenu>> getRoleMenus(@PathVariable Long roleId) {
        return sysMenuService.getByRoleId(roleId);
    }

    /**
     * 分配角色菜单
     * 先删除该角色原有的菜单关联，再批量插入新的关联记录
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     * @return 操作结果
     */
    @PreAuthorize("@ps.hasPermission('sys:role:add')")
    @PostMapping("/{roleId}/menus")
    public R<Void> assignRoleMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        return sysMenuService.assignMenus(roleId, menuIds);
    }
}
