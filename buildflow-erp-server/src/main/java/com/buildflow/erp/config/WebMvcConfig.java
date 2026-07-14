package com.buildflow.erp.config;

import com.buildflow.erp.filter.TraceIdFilter;
import com.buildflow.erp.filter.XssFilter;
import com.buildflow.erp.interceptor.IdempotentInterceptor;
import com.buildflow.erp.interceptor.RateLimitInterceptor;
import com.buildflow.erp.interceptor.SignatureInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 注册限流、幂等性、签名验证拦截器及XSS过滤器，统一管控接口安全策略
 *
 * <p>拦截器执行顺序（按注册顺序）：
 * <ol>
 *   <li>SignatureInterceptor — 签名验证，防止请求篡改和重放</li>
 *   <li>RateLimitInterceptor — 限流控制，防止接口被恶意刷取</li>
 *   <li>IdempotentInterceptor — 幂等校验，防止重复提交</li>
 * </ol></p>
 *
 * <p>拦截范围：/auth/** 及所有业务接口，排除 Knife4j 文档相关路径。
 * 各拦截器内部会检查对应注解，无注解的方法自动放行。</p>
 *
 * @see RateLimitInterceptor
 * @see IdempotentInterceptor
 * @see SignatureInterceptor
 * @see XssFilter
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    private IdempotentInterceptor idempotentInterceptor;

    @Autowired
    private SignatureInterceptor signatureInterceptor;

    /** Knife4j 文档排除路径 */
    private static final String[] EXCLUDE_PATHS = {
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v2/api-docs"
    };

    /**
     * 注册拦截器
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 签名验证拦截器（最高优先级，先验证请求合法性）
        registry.addInterceptor(signatureInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS)
                .order(1);

        // 2. 限流拦截器
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS)
                .order(2);

        // 3. 幂等性拦截器
        registry.addInterceptor(idempotentInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS)
                .order(3);
    }

    /**
     * 注册XSS过滤器
     * 设置最高优先级，确保在Spring Security过滤器之前包装请求
     * @return FilterRegistrationBean XSS过滤器注册Bean
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        // 最高优先级，先于其他过滤器执行
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    /**
     * 注册TraceId过滤器
     * 紧随XSS过滤器之后执行，确保请求链路中尽早写入traceId到MDC，
     * 使后续所有日志都能输出traceId实现链路追踪。
     * @return FilterRegistrationBean TraceId过滤器注册Bean
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.addUrlPatterns("/*");
        registration.setName("traceIdFilter");
        // 紧随XSS过滤器之后执行
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
}
