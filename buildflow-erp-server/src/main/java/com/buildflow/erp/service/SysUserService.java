package com.buildflow.erp.service;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SysUser;

/**
 * 用户服务接口
 * 定义用户管理的核心业务方法，包括分页查询、增删改查、密码重置、状态修改等操作。
 */
public interface SysUserService {

    /**
     * 分页查询用户列表
     * 支持按用户名模糊搜索和账号状态过滤
     *
     * @param pageNum  当前页码，从1开始
     * @param pageSize 每页记录数
     * @param username 用户名（模糊查询），可为null
     * @param status   账号状态（0-正常 1-禁用），可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页用户列表
     */
    R<PageResult<SysUser>> page(Integer pageNum, Integer pageSize, String username, Integer status);

    /**
     * 新增用户
     * 默认密码为123456，使用MD5加密后存储
     *
     * @param user 用户实体对象，需包含username、realName、phone、roleId等字段
     * @return 操作结果，成功返回R.ok()，用户名已存在返回R.fail()
     */
    R<Void> add(SysUser user);

    /**
     * 编辑用户信息
     * 不允许修改用户名和密码
     *
     * @param user 用户实体对象，id字段必填
     * @return 操作结果，成功返回R.ok()，用户不存在返回R.fail()
     */
    R<Void> update(SysUser user);

    /**
     * 删除用户（逻辑删除）
     * 删除前会检查用户是否有关联业务数据
     *
     * @param id 用户ID
     * @return 操作结果，成功返回R.ok()，存在关联数据时返回R.fail()
     */
    R<Void> delete(Long id);

    /**
     * 重置用户密码
     * 将密码重置为默认密码123456，使用MD5加密后更新
     *
     * @param id 用户ID
     * @return 操作结果，成功返回R.ok()，用户不存在返回R.fail()
     */
    R<Void> resetPassword(Long id);

    /**
     * 修改账号状态
     * 用于启用或禁用用户账号
     *
     * @param id     用户ID
     * @param status 账号状态（0-正常 1-禁用）
     * @return 操作结果，成功返回R.ok()，用户不存在返回R.fail()
     */
    R<Void> updateStatus(Long id, Integer status);

    /**
     * 根据用户名查询用户
     * 用于登录认证等场景
     *
     * @param username 登录用户名
     * @return 匹配的用户实体对象，未找到时返回null
     */
    SysUser getByUsername(String username);
}
