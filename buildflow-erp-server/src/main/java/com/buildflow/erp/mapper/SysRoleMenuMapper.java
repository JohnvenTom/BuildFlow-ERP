package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色菜单关联Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供SysRoleMenu实体的基础CRUD操作，
 * 用于维护角色与菜单的多对多关联关系。
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
}
