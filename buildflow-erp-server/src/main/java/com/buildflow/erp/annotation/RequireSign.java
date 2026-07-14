package com.buildflow.erp.annotation;

import java.lang.annotation.*;

/**
 * 接口签名验证注解
 * 要求请求携带 appkey + timestamp + nonce + sign 进行签名校验，防止请求被篡改和重放
 *
 * <p>使用方式：在需要签名验证的Controller方法上标注 @RequireSign
 * 请求头需携带 X-AppKey、X-Timestamp、X-Nonce、X-Sign 四个字段。</p>
 *
 * <p>签名算法：MD5(appkey + timestamp + nonce + secret)</p>
 *
 * @see com.buildflow.erp.interceptor.SignatureInterceptor
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireSign {
}
