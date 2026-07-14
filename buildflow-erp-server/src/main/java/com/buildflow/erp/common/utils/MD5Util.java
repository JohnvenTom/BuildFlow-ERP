package com.buildflow.erp.common.utils;

import org.springframework.util.DigestUtils;

/**
 * MD5加密工具类
 * 提供密码加密和验证功能，采用MD5加盐方式保障密码安全
 */
public class MD5Util {

    /** 盐值，用于增强密码加密安全性 */
    private static final String SALT = "buildflow_erp_2026";

    /**
     * MD5加盐加密
     * @param password 原始密码
     * @return 加密后的密码字符串
     */
    public static String encrypt(String password) {
        String salted = SALT + password + SALT;
        return DigestUtils.md5DigestAsHex(salted.getBytes());
    }

    /**
     * 验证密码是否匹配
     * @param rawPassword 原始密码
     * @param encryptedPassword 加密后的密码
     * @return 匹配返回true，不匹配返回false
     */
    public static boolean verify(String rawPassword, String encryptedPassword) {
        return encrypt(rawPassword).equals(encryptedPassword);
    }
}
