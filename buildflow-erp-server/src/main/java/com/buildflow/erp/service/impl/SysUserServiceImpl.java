package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.common.utils.PasswordUtil;
import com.buildflow.erp.entity.SysOperationLog;
import com.buildflow.erp.entity.SysUser;
import com.buildflow.erp.mapper.SysUserMapper;
import com.buildflow.erp.service.SysOperationLogService;
import com.buildflow.erp.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现类
 * 实现用户管理的核心业务逻辑，包括分页查询、新增、编辑、删除、密码重置、状态修改等操作。
 * 所有增删改操作均记录操作日志。
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysOperationLogService sysOperationLogService;

    /**
     * 分页查询用户列表
     * 支持按用户名模糊搜索和账号状态过滤，使用MyBatis-Plus的Page对象实现分页
     *
     * @param pageNum  当前页码，从1开始
     * @param pageSize 每页记录数
     * @param username 用户名（模糊查询），可为null
     * @param status   账号状态，可为null表示不过滤
     * @return 分页结果，包含总记录数和当前页用户列表
     */
    @Override
    public R<PageResult<SysUser>> page(Integer pageNum, Integer pageSize, String username, Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysUser::getUsername, username)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = sysUserMapper.selectPage(page, wrapper);
        return R.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    /**
     * 新增用户
     * 校验用户名唯一性，默认密码为123456并使用MD5加密存储
     *
     * @param user 用户实体对象，需包含username、realName、phone、roleId等字段
     * @return 操作结果，用户名已存在时返回R.fail()
     */
    @Override
    public R<Void> add(SysUser user) {
        // 校验用户名是否已存在
        SysUser existing = sysUserMapper.selectByUsername(user.getUsername());
        if (existing != null) {
            return R.fail("用户名已存在");
        }
        // 设置默认密码123456并加密
        user.setPassword(PasswordUtil.encrypt("123456"));
        user.setStatus(Constants.USER_STATUS_NORMAL);
        sysUserMapper.insert(user);

        // 记录操作日志
        saveOperationLog("用户管理", "新增用户：" + user.getUsername());
        return R.ok();
    }

    /**
     * 编辑用户信息
     * 不允许修改用户名和密码，仅更新realName、phone、roleId等字段
     *
     * @param user 用户实体对象，id字段必填
     * @return 操作结果，用户不存在时返回R.fail()
     */
    @Override
    public R<Void> update(SysUser user) {
        SysUser existing = sysUserMapper.selectById(user.getId());
        if (existing == null) {
            return R.fail("用户不存在");
        }
        // 不允许修改用户名和密码
        user.setUsername(null);
        user.setPassword(null);
        sysUserMapper.updateById(user);

        // 记录操作日志
        saveOperationLog("用户管理", "编辑用户：" + existing.getUsername());
        return R.ok();
    }

    /**
     * 删除用户（逻辑删除）
     * 删除前检查用户是否有关联业务数据（当前检查同一用户是否还存在，由逻辑删除机制处理）
     *
     * @param id 用户ID
     * @return 操作结果，用户不存在时返回R.fail()
     */
    @Override
    public R<Void> delete(Long id) {
        SysUser existing = sysUserMapper.selectById(id);
        if (existing == null) {
            return R.fail("用户不存在");
        }
        // 检查用户是否有关联业务数据（可根据业务需要扩展检查逻辑）
        // 当前仅执行逻辑删除，MyBatis-Plus的@TableLogic会自动处理
        sysUserMapper.deleteById(id);

        // 记录操作日志
        saveOperationLog("用户管理", "删除用户：" + existing.getUsername());
        return R.ok();
    }

    /**
     * 重置用户密码
     * 将密码重置为默认密码123456，使用MD5加密后更新
     *
     * @param id 用户ID
     * @return 操作结果，用户不存在时返回R.fail()
     */
    @Override
    public R<Void> resetPassword(Long id) {
        SysUser existing = sysUserMapper.selectById(id);
        if (existing == null) {
            return R.fail("用户不存在");
        }
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, id)
                .set(SysUser::getPassword, PasswordUtil.encrypt("123456"))
                .set(SysUser::getLoginFailCount, 0)
                .set(SysUser::getLockedUntil, null);
        sysUserMapper.update(null, wrapper);

        // 记录操作日志
        saveOperationLog("用户管理", "重置密码：" + existing.getUsername());
        return R.ok();
    }

    /**
     * 修改账号状态
     * 用于启用或禁用用户账号
     *
     * @param id     用户ID
     * @param status 账号状态（0-正常 1-禁用）
     * @return 操作结果，用户不存在时返回R.fail()
     */
    @Override
    public R<Void> updateStatus(Long id, Integer status) {
        SysUser existing = sysUserMapper.selectById(id);
        if (existing == null) {
            return R.fail("用户不存在");
        }
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, id).set(SysUser::getStatus, status);
        sysUserMapper.update(null, wrapper);

        // 记录操作日志
        String statusText = Constants.USER_STATUS_NORMAL.equals(status) ? "启用" : "禁用";
        saveOperationLog("用户管理", statusText + "账号：" + existing.getUsername());
        return R.ok();
    }

    /**
     * 根据用户名查询用户
     * 用于登录认证等场景
     *
     * @param username 登录用户名
     * @return 匹配的用户实体对象，未找到时返回null
     */
    @Override
    public SysUser getByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    /**
     * 保存操作日志的内部辅助方法
     * 构建日志对象并异步保存
     *
     * @param module    操作模块名称
     * @param operation 操作描述
     */
    private void saveOperationLog(String module, String operation) {
        SysOperationLog log = new SysOperationLog();
        log.setModule(module);
        log.setOperation(operation);
        sysOperationLogService.save(log);
    }
}
