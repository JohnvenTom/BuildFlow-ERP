package com.buildflow.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SysOperationLog;
import com.buildflow.erp.entity.SysRole;
import com.buildflow.erp.entity.SysUser;
import com.buildflow.erp.mapper.SysRoleMapper;
import com.buildflow.erp.mapper.SysUserMapper;
import com.buildflow.erp.service.SysOperationLogService;
import com.buildflow.erp.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色服务实现类
 * 实现角色管理的核心业务逻辑，包括角色列表查询、新增、编辑、删除等操作。
 * 删除角色时会校验是否已分配给用户，所有增删改操作均记录操作日志。
 */
@Service
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysOperationLogService sysOperationLogService;

    /**
     * 查询全部角色列表
     * 返回系统中所有未删除的角色，按创建时间降序排列
     *
     * @return 角色列表
     */
    @Override
    public R<List<SysRole>> list() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysRole::getCreateTime);
        List<SysRole> list = sysRoleMapper.selectList(wrapper);
        return R.ok(list);
    }

    /**
     * 新增角色
     * 新增前校验角色编码是否已存在，保证编码唯一性
     *
     * @param role 角色实体对象，需包含roleName、roleCode字段
     * @return 操作结果，角色编码已存在时返回R.fail()
     */
    @Override
    public R<Void> add(SysRole role) {
        // 校验角色编码是否已存在
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, role.getRoleCode());
        if (sysRoleMapper.selectCount(wrapper) > 0) {
            return R.fail("角色编码已存在");
        }
        sysRoleMapper.insert(role);

        // 记录操作日志
        saveOperationLog("角色管理", "新增角色：" + role.getRoleName());
        return R.ok();
    }

    /**
     * 编辑角色信息
     * 不允许修改角色编码，仅更新roleName、description等字段
     *
     * @param role 角色实体对象，id字段必填
     * @return 操作结果，角色不存在时返回R.fail()
     */
    @Override
    public R<Void> update(SysRole role) {
        SysRole existing = sysRoleMapper.selectById(role.getId());
        if (existing == null) {
            return R.fail("角色不存在");
        }
        // 不允许修改角色编码
        role.setRoleCode(null);
        sysRoleMapper.updateById(role);

        // 记录操作日志
        saveOperationLog("角色管理", "编辑角色：" + existing.getRoleName());
        return R.ok();
    }

    /**
     * 删除角色
     * 删除前检查角色是否已分配给用户，若存在关联用户则不允许删除
     *
     * @param id 角色ID
     * @return 操作结果，角色已分配给用户时返回R.fail()
     */
    @Override
    public R<Void> delete(Long id) {
        SysRole existing = sysRoleMapper.selectById(id);
        if (existing == null) {
            return R.fail("角色不存在");
        }
        // 检查角色是否已分配给用户
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getRoleId, id);
        if (sysUserMapper.selectCount(userWrapper) > 0) {
            return R.fail("该角色已分配给用户，无法删除");
        }
        sysRoleMapper.deleteById(id);

        // 记录操作日志
        saveOperationLog("角色管理", "删除角色：" + existing.getRoleName());
        return R.ok();
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
