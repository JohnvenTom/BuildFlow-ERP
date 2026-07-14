package com.buildflow.erp.dto;

import lombok.Data;

import java.util.List;

/**
 * 登录响应DTO
 * 封装登录成功后返回给前端的认证信息及用户基础数据，供前端存储和权限渲染使用
 */
@Data
public class LoginResponse {

    /** JWT令牌，前端需存储并在后续请求头中携带 */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 登录用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 角色ID */
    private Long roleId;

    /** 角色名称 */
    private String roleName;

    /** 当前用户拥有的菜单权限树 */
    private List<MenuVO> menus;
}
