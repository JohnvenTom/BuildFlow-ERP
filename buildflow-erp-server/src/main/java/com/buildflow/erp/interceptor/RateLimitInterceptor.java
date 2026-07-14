package com.buildflow.erp.interceptor;

import com.buildflow.erp.annotation.RateLimit;
import com.buildflow.erp.common.result.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流拦截器
 * 基于IP+接口URI+时间窗口进行请求限流控制，防止接口被恶意刷取
 *
 * <p>实现原理：
 * <ul>
 *   <li>使用 ConcurrentHashMap 存储每个 IP+URI 的请求时间戳列表</li>
 *   <li>每次请求清理超过时间窗口的过期记录</li>
 *   <li>当前窗口内请求数达到上限时返回 429 状态码</li>
 *   <li>登录接口（/auth/login）默认限流更严格：5次/分钟</li>
 * </ul></p>
 *
 * <p>注意事项：基于内存存储，重启后限流记录清空；分布式部署需替换为 Redis 实现</p>
 *
 * @see RateLimit
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    /** 限流记录容器：key = IP:URI，value = 请求时间戳列表 */
    private final ConcurrentHashMap<String, List<Long>> requestRecords = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 登录接口URI标识 */
    private static final String LOGIN_URI = "/auth/login";
    /** 登录接口默认时间窗口（秒） */
    private static final int LOGIN_TIME_WINDOW = 60;
    /** 登录接口默认最大请求次数 */
    private static final int LOGIN_MAX_COUNT = 5;
    /** HTTP 429 Too Many Requests 状态码 */
    private static final int SC_TOO_MANY_REQUESTS = 429;

    /**
     * 请求预处理，执行限流检查
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器对象
     * @return true放行，false拒绝请求
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 非Controller方法（如静态资源）直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        int timeWindow;
        int maxCount;
        String message;

        if (rateLimit != null) {
            // 使用注解配置
            timeWindow = rateLimit.timeWindow();
            maxCount = rateLimit.maxCount();
            message = rateLimit.message();
        } else if (LOGIN_URI.equals(request.getRequestURI())) {
            // 登录接口无注解时使用严格默认限流
            timeWindow = LOGIN_TIME_WINDOW;
            maxCount = LOGIN_MAX_COUNT;
            message = "登录尝试过于频繁，请稍后再试";
        } else {
            // 无限流注解且非登录接口，放行
            return true;
        }

        // 获取客户端IP
        String ip = getClientIp(request);
        String uri = request.getRequestURI();
        String key = ip + ":" + uri;

        // 限流检查
        if (isRateLimited(key, timeWindow, maxCount)) {
            log.warn("接口限流触发，IP={}, URI={}, 时间窗口={}秒, 上限={}次", ip, uri, timeWindow, maxCount);
            writeErrorResponse(response, SC_TOO_MANY_REQUESTS, message);
            return false;
        }

        return true;
    }

    /**
     * 判断当前请求是否被限流
     * @param key 限流键（IP:URI）
     * @param timeWindow 时间窗口（秒）
     * @param maxCount 最大请求次数
     * @return true表示已被限流（拒绝），false表示允许通过
     */
    private boolean isRateLimited(String key, int timeWindow, int maxCount) {
        long now = System.currentTimeMillis();
        long windowStart = now - timeWindow * 1000L;

        // 使用 compute 保证线程安全
        List<Long> timestamps = requestRecords.compute(key, (k, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }
            // 添加当前请求时间戳
            list.add(now);
            return list;
        });

        // 清理过期记录并统计有效请求数
        synchronized (timestamps) {
            Iterator<Long> iterator = timestamps.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() < windowStart) {
                    iterator.remove();
                }
            }
            int count = timestamps.size();
            // 当前窗口内请求数超过上限（含本次请求）
            return count > maxCount;
        }
    }

    /**
     * 获取客户端真实IP地址
     * 依次从 X-Forwarded-For、X-Real-IP、Proxy-Client-IP、WL-Proxy-Client-IP 请求头获取，
     * 都取不到时使用 remoteAddr
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
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
