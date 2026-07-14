package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供SysRole实体的基础CRUD操作。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
