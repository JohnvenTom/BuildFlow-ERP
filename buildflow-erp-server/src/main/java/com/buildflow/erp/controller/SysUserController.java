package com.buildflow.erp.controller;

import com.buildflow.erp.common.result.PageResult;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.entity.SysUser;
import com.buildflow.erp.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户管理控制器
 * 提供系统用户的分页查询、新增、编辑、删除、密码重置及状态修改等接口
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 分页查询用户列表
     * 支持按用户名模糊搜索和账号状态过滤
     *
     * @param pageNum  当前页码，从1开始
     * @param pageSize 每页记录数
     * @param username 用户名（模糊查询），可为空
     * @param status   账号状态（0-正常 1-禁用），可为空
     * @return 分页结果，包含总记录数和当前页用户列表
     */
    @GetMapping("/page")
    public R<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        return sysUserService.page(pageNum, pageSize, username, status);
    }

    /**
     * 新增用户
     * 默认密码为123456，新增前会校验用户名是否已存在
     *
     * @param user 用户实体对象，需包含username、realName、phone、roleId等字段
     * @return 操作结果
     */
    @PostMapping
    public R<Void> add(@RequestBody SysUser user) {
        return sysUserService.add(user);
    }

    /**
     * 编辑用户信息
     * 不允许修改用户名和密码
     *
     * @param user 用户实体对象，id字段必填
     * @return 操作结果
     */
    @PutMapping
    public R<Void> update(@RequestBody SysUser user) {
        return sysUserService.update(user);
    }

    /**
     * 删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        return sysUserService.delete(id);
    }

    /**
     * 重置用户密码
     * 将密码重置为默认密码123456
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id) {
        return sysUserService.resetPassword(id);
    }

    /**
     * 修改账号状态
     * 用于启用或禁用用户账号
     *
     * @param id      用户ID
     * @param status  账号状态（0-正常 1-禁用）
     * @param request HTTP请求对象，用于获取当前登录用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest request) {
        return sysUserService.updateStatus(id, status);
    }
}
