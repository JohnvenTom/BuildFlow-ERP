package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SysMenu;
import com.buildflow.erp.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单控制器
 * 提供菜单树查询接口，用于菜单管理页面及角色菜单分配
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 查询菜单树
     * 返回所有菜单并按parentId组装为树形结构，用于菜单管理页面展示
     *
     * @return 树形结构的菜单列表，顶级菜单的parentId为0
     */
    @PreAuthorize("@ps.hasPermission('sys:menu:list')")
    @GetMapping("/tree")
    public R<List<SysMenu>> tree() {
        return sysMenuService.tree();
    }
}
