package com.buildflow.erp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 限制允许的源地址，避免任意域名携带凭证访问
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://localhost:3001,http://127.0.0.1:5173}")
    private String allowedOrigins;

    /**
     * 注册跨域过滤器
     * 仅允许配置的源地址访问，限制方法和头部
     * @return CorsFilter 跨域过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 从配置读取允许的源地址，使用 OriginPattern 支持通配符
        for (String origin : allowedOrigins.split(",")) {
            config.addAllowedOriginPattern(origin.trim());
        }
        config.setAllowCredentials(true);
        // 仅允许必要的HTTP方法
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        // 仅允许必要的请求头
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("Idempotent-Key");
        config.addAllowedHeader("X-AppKey");
        config.addAllowedHeader("X-Timestamp");
        config.addAllowedHeader("X-Nonce");
        config.addAllowedHeader("X-Sign");
        config.addExposedHeader("Authorization");
        // 预检请求缓存1小时
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
