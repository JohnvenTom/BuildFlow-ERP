package com.buildflow.erp.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 封装用户登录时提交的认证参数，用于登录接口入参校验
 */
@Data
public class LoginRequest {

    /** 登录用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 登录密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
