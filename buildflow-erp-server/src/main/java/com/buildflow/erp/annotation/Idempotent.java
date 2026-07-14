package com.buildflow.erp.annotation;

import java.lang.annotation.*;

/**
 * 接口幂等性注解
 * 确保同一请求在指定时间内不会被重复处理，防止前端重复提交导致的脏数据
 *
 * <p>使用方式：在需要幂等控制的Controller方法上标注 @Idempotent(expire = 30)
 * 表示30秒内相同幂等键的请求只会被处理一次，重复请求返回"请勿重复提交"。</p>
 *
 * @see com.buildflow.erp.interceptor.IdempotentInterceptor
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等过期时间（秒），默认30秒
     * @return 过期时间秒数
     */
    int expire() default 30;

    /**
     * 幂等键的字段名（从请求头或参数中获取），默认从 Header 的 Idempotent-Key 获取
     * @return 幂等键字段名
     */
    String keyField() default "Idempotent-Key";
}
