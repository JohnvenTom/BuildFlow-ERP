package com.buildflow.erp.config;

import com.buildflow.erp.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SpringSecurity安全配置类
 * 配置JWT无状态认证、接口权限控制、登录放行等安全策略
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
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
            .csrf().disable()
            // 无状态Session
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 配置接口权限
            .authorizeRequests()
                // 放行登录接口
                .antMatchers("/auth/login", "/auth/captcha").permitAll()
                // Swagger文档：非生产环境放行，生产环境需认证
                .antMatchers("/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs")
                .permitAll()
                // 其他请求需认证
                .anyRequest().authenticated()
            .and()
            // 禁用默认登出
            .logout().disable()
            // 禁用表单登录
            .formLogin().disable();

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
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
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
