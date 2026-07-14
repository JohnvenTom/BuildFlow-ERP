package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供SysMenu实体的基础CRUD操作，
 * 并扩展根据角色ID查询关联菜单列表的自定义方法。
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据角色ID查询菜单列表
     * 通过sys_role_menu关联表查询指定角色所拥有的所有菜单
     *
     * @param roleId 角色ID，关联sys_role.id
     * @return 该角色关联的菜单列表，无关联时返回空列表
     */
    List<SysMenu> selectByRoleId(@Param("roleId") Long roleId);
}
