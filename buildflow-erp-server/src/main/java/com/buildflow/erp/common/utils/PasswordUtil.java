package com.buildflow.erp.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类
 * 使用BCrypt算法进行密码加密和验证，替代不安全的MD5方案
 * BCrypt自带随机盐，每次加密结果不同，有效防止彩虹表攻击
 */
public class PasswordUtil {

    /** BCrypt加密器实例 */
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    /**
     * 加密密码（BCrypt自动生成随机盐）
     * @param rawPassword 原始密码
     * @return BCrypt加密后的密码字符串
     */
    public static String encrypt(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码是否匹配
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 匹配返回true，不匹配返回false
     */
    public static boolean verify(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
