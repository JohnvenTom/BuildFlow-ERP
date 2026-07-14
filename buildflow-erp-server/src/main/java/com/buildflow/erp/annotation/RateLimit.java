package com.buildflow.erp.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 基于IP+接口+时间窗口进行限流控制，防止恶意刷接口
 *
 * <p>使用方式：在需要限流的Controller方法上标注 @RateLimit(timeWindow = 60, maxCount = 10)
 * 表示60秒内同一IP对该接口最多访问10次，超出返回429状态码。</p>
 *
 * @see com.buildflow.erp.interceptor.RateLimitInterceptor
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 时间窗口（秒），默认60秒
     * @return 时间窗口秒数
     */
    int timeWindow() default 60;

    /**
     * 时间窗口内最大请求次数
     * @return 最大请求次数
     */
    int maxCount() default 10;

    /**
     * 限流提示消息
     * @return 限流时返回的提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}
