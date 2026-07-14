package com.buildflow.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buildflow.erp.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供SysUser实体的基础CRUD操作，
 * 并扩展根据用户名查询用户的自定义方法。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     *
     * @param username 登录用户名，唯一标识
     * @return 匹配的用户实体对象，未找到时返回null
     */
    SysUser selectByUsername(String username);
}
