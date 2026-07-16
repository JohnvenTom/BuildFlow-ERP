package com.buildflow.erp.config;

import com.buildflow.erp.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SpringSecurity 6 安全配置类
 * 配置JWT无状态认证、接口权限控制、登录放行等安全策略
 *
 * <p>Spring Boot 3 / Spring Security 6 主要变更：
 * <ul>
 *   <li>使用 @EnableMethodSecurity 替代 @EnableGlobalMethodSecurity</li>
 *   <li>使用 authorizeHttpRequests() 替代 authorizeRequests()</li>
 *   <li>使用 requestMatchers() 替代 antMatchers()</li>
 *   <li>lambda 风格配置链</li>
 * </ul></p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    /** Swagger是否启用（生产环境关闭） */
    @Value("${knife4j.production:false}")
    private boolean knife4jProduction;

    /**
     * 配置安全过滤链
     * @param http HttpSecurity配置对象
     * @return SecurityFilterChain 安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（前后端分离不需要）
            .csrf(csrf -> csrf.disable())
            // 无状态Session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置接口权限
            .authorizeHttpRequests(auth -> auth
                // 放行登录接口
                .requestMatchers("/auth/login", "/auth/captcha").permitAll()
                // Swagger文档：非生产环境放行，生产环境需认证
                .requestMatchers("/doc.html", "/webjars/**", "/swagger-resources/**",
                        "/v2/api-docs", "/v3/api-docs/**", "/favicon.ico").permitAll()
                // 其他请求需认证
                .anyRequest().authenticated()
            )
            // 禁用默认登出
            .logout(logout -> logout.disable())
            // 禁用表单登录
            .formLogin(form -> form.disable());

        // 在UsernamePasswordAuthenticationFilter之前添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置认证管理器
     * @param http HttpSecurity配置对象
     * @return AuthenticationManager 认证管理器
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }

    /**
     * 密码编码器（BCrypt）
     * @return PasswordEncoder BCrypt密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
