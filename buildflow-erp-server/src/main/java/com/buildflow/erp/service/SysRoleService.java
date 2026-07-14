package com.buildflow.erp.service;

import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 * 定义角色管理的核心业务方法，包括角色列表查询、增删改等操作。
 */
public interface SysRoleService {

    /**
     * 查询全部角色列表
     * 返回系统中所有未删除的角色，通常用于角色下拉选择
     *
     * @return 角色列表
     */
    R<List<SysRole>> list();

    /**
     * 新增角色
     * 新增前会校验角色编码是否已存在
     *
     * @param role 角色实体对象，需包含roleName、roleCode字段
     * @return 操作结果，成功返回R.ok()，角色编码已存在返回R.fail()
     */
    R<Void> add(SysRole role);

    /**
     * 编辑角色信息
     * 不允许修改角色编码
     *
     * @param role 角色实体对象，id字段必填
     * @return 操作结果，成功返回R.ok()，角色不存在返回R.fail()
     */
    R<Void> update(SysRole role);

    /**
     * 删除角色
     * 删除前会检查角色是否已分配给用户
     *
     * @param id 角色ID
     * @return 操作结果，成功返回R.ok()，角色已分配给用户时返回R.fail()
     */
    R<Void> delete(Long id);
}
