package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SysMenu;
import com.buildflow.erp.entity.SysRoleMenu;
import com.buildflow.erp.mapper.SysMenuMapper;
import com.buildflow.erp.mapper.SysRoleMenuMapper;
import com.buildflow.erp.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 * 实现菜单管理的核心业务逻辑，包括菜单树查询、角色菜单查询、菜单分配等操作。
 * 菜单分配操作使用事务保证数据一致性。
 */
@Service
public class SysMenuServiceImpl implements SysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 查询全部菜单树
     * 查询所有菜单并按parentId组装为树形结构，顶级菜单的parentId为0
     *
     * @return 树形结构的菜单列表
     */
    @Override
    public R<List<SysMenu>> tree() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysMenu::getSort);
        List<SysMenu> allMenus = sysMenuMapper.selectList(wrapper);
        List<SysMenu> tree = buildTree(allMenus, 0L);
        return R.ok(tree);
    }

    /**
     * 根据角色ID获取菜单列表
     * 通过sys_role_menu关联表查询指定角色所拥有的菜单
     *
     * @param roleId 角色ID，关联sys_role.id
     * @return 该角色关联的菜单列表
     */
    @Override
    public R<List<SysMenu>> getByRoleId(Long roleId) {
        List<SysMenu> menus = sysMenuMapper.selectByRoleId(roleId);
        return R.ok(menus);
    }

    /**
     * 分配角色菜单
     * 采用先删后增策略：先删除该角色原有的菜单关联，再批量插入新的关联记录。
     * 使用事务保证操作原子性。
     *
     * @param roleId  角色ID，关联sys_role.id
     * @param menuIds 菜单ID列表，关联sys_menu.id
     * @return 操作结果，成功返回R.ok()
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> assignMenus(Long roleId, List<Long> menuIds) {
        // 删除该角色原有的菜单关联
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        sysRoleMenuMapper.delete(wrapper);

        // 批量插入新的关联记录
        if (!CollectionUtils.isEmpty(menuIds)) {
            List<SysRoleMenu> roleMenus = menuIds.stream().map(menuId -> {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                return roleMenu;
            }).collect(Collectors.toList());
            roleMenus.forEach(sysRoleMenuMapper::insert);
        }
        return R.ok();
    }

    /**
     * 构建菜单树形结构的内部辅助方法
     * 递归将扁平菜单列表组装为树形结构，设置每个菜单的children属性
     *
     * @param allMenus 所有菜单的扁平列表
     * @param parentId 当前层级的父菜单ID
     * @return 以parentId为根的子菜单树列表
     */
    private List<SysMenu> buildTree(List<SysMenu> allMenus, Long parentId) {
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : allMenus) {
            if (parentId.equals(menu.getParentId())) {
                // 递归查找子菜单并设置到children属性
                List<SysMenu> children = buildTree(allMenus, menu.getId());
                menu.setChildren(children.isEmpty() ? null : children);
                tree.add(menu);
            }
        }
        return tree;
    }
}
