package com.buildflow.erp.common.security;

import com.buildflow.erp.entity.SysRoleMenu;
import com.buildflow.erp.entity.SysMenu;
import com.buildflow.erp.mapper.SysMenuMapper;
import com.buildflow.erp.mapper.SysRoleMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限校验服务
 * 提供基于角色-菜单权限的方法级权限校验
 */
@Service("ps")
public class PermissionService {

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 校验当前用户是否拥有指定权限
     * @param permission 权限标识，如 crm:customer:list
     * @return 有权限返回true，无权限返回false
     */
    public boolean hasPermission(String permission) {
        // 从Security上下文获取角色ID
        Long roleId = getRoleIdFromContext();
        if (roleId == null) {
            return false;
        }
        // 管理员角色（roleId=1）拥有全部权限
        if (roleId == 1L) {
            return true;
        }
        // 查询角色拥有的菜单
        List<SysMenu> menus = sysMenuMapper.selectByRoleId(roleId);
        // 校验权限标识是否匹配
        return menus.stream()
                .anyMatch(menu -> permission.equals(menu.getPermission()));
    }

    /**
     * 从Security上下文获取角色ID
     * @return 角色ID，未认证返回null
     */
    private Long getRoleIdFromContext() {
        try {
            Object details = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getDetails();
            if (details instanceof Long) {
                return (Long) details;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
