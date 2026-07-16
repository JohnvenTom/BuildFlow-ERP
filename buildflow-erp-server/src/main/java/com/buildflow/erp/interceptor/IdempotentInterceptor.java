package com.buildflow.erp.interceptor;

import com.buildflow.erp.annotation.Idempotent;
import com.buildflow.erp.common.result.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 幂等性拦截器
 * 确保同一请求在指定时间内不会被重复处理，防止前端重复提交导致脏数据
 *
 * <p>实现原理：
 * <ul>
 *   <li>从请求头获取幂等键（默认 Idempotent-Key）</li>
 *   <li>POST/PUT 请求若无幂等键，基于 URI+参数MD5 自动生成</li>
 *   <li>使用 ConcurrentHashMap 存储已处理的幂等键及过期时间戳</li>
 *   <li>键已存在且未过期时返回"请勿重复提交"</li>
 *   <li>正常放行后将键存入Map，并定期清理过期记录</li>
 * </ul></p>
 *
 * <p>注意事项：
 * <ul>
 *   <li>基于内存存储，重启后幂等记录清空；分布式部署需替换为 Redis 实现</li>
 *   <li>JSON请求体场景建议前端显式传 Idempotent-Key，避免自动生成时无法读取body</li>
 * </ul></p>
 *
 * @see Idempotent
 */
@Component
public class IdempotentInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(IdempotentInterceptor.class);

    /** 幂等记录容器：key = 幂等键，value = 过期时间戳（毫秒） */
    private final ConcurrentHashMap<String, Long> idempotentKeys = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** HTTP 409 Conflict 状态码 */
    private static final int SC_CONFLICT = 409;

    /**
     * 请求预处理，执行幂等性校验
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器对象
     * @return true放行，false拒绝重复请求
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 非Controller方法直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Idempotent idempotent = handlerMethod.getMethodAnnotation(Idempotent.class);

        // 无幂等注解，放行
        if (idempotent == null) {
            return true;
        }

        int expire = idempotent.expire();
        String keyField = idempotent.keyField();

        // 获取幂等键
        String idempotentKey = request.getHeader(keyField);

        // 无幂等键时，POST/PUT 自动生成
        String method = request.getMethod();
        if (!StringUtils.hasText(idempotentKey) && ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method))) {
            idempotentKey = generateKey(request);
        }

        // 仍无幂等键，放行（幂等键非强制）
        if (!StringUtils.hasText(idempotentKey)) {
            return true;
        }

        long now = System.currentTimeMillis();
        long expireTime = now + expire * 1000L;

        // 清理过期记录
        cleanExpiredKeys(now);

        // 检查是否已存在且未过期
        Long existingExpire = idempotentKeys.get(idempotentKey);
        if (existingExpire != null && existingExpire > now) {
            log.warn("重复提交被拦截，幂等键={}", idempotentKey);
            writeErrorResponse(response, SC_CONFLICT, "请勿重复提交");
            return false;
        }

        // 存入幂等键
        idempotentKeys.put(idempotentKey, expireTime);
        log.debug("幂等键已记录，key={}, 过期时间={}ms", idempotentKey, expireTime);
        return true;
    }

    /**
     * 基于请求URI+方法+参数自动生成幂等键
     * 对 URI + HTTP方法 + 查询参数 进行MD5摘要作为幂等键
     * @param request HTTP请求
     * @return MD5格式的幂等键
     */
    private String generateKey(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getRequestURI());
        sb.append(request.getMethod());
        // 拼接查询参数和表单参数
        Map<String, String[]> paramMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            sb.append(entry.getKey());
            for (String val : entry.getValue()) {
                sb.append(val);
            }
        }
        return DigestUtils.md5DigestAsHex(sb.toString().getBytes());
    }

    /**
     * 清理已过期的幂等键记录
     * @param now 当前时间戳（毫秒）
     */
    private void cleanExpiredKeys(long now) {
        Iterator<Map.Entry<String, Long>> iterator = idempotentKeys.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (entry.getValue() <= now) {
                iterator.remove();
            }
        }
    }

    /**
     * 写入错误响应JSON
     * @param response HTTP响应
     * @param status HTTP状态码
     * @param message 错误消息
     * @throws Exception 写入异常
     */
    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(R.fail(status, message)));
    }
}
