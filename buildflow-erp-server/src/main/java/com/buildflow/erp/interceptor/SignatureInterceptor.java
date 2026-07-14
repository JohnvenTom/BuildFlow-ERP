package com.buildflow.erp.interceptor;

import com.buildflow.erp.annotation.RequireSign;
import com.buildflow.erp.common.result.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口签名验证拦截器
 * 校验请求携带的 appkey + timestamp + nonce + sign 签名，防止请求被篡改和重放攻击
 *
 * <p>签名算法：MD5(appkey + timestamp + nonce + secret)</p>
 *
 * <p>校验流程：
 * <ul>
 *   <li>1. 检查请求头是否携带 X-AppKey、X-Timestamp、X-Nonce、X-Sign</li>
 *   <li>2. 校验 timestamp 是否在有效期内（默认5分钟）</li>
 *   <li>3. 校验 nonce 是否已被使用（防重放）</li>
 *   <li>4. 按相同算法计算签名并比对</li>
 * </ul></p>
 *
 * <p>注意事项：nonce 防重放基于内存存储，重启后清空；分布式部署需替换为 Redis 实现</p>
 *
 * @see RequireSign
 */
@Component
public class SignatureInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SignatureInterceptor.class);

    /** 请求头：应用密钥标识 */
    private static final String HEADER_APP_KEY = "X-AppKey";
    /** 请求头：时间戳 */
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    /** 请求头：随机数 */
    private static final String HEADER_NONCE = "X-Nonce";
    /** 请求头：签名 */
    private static final String HEADER_SIGN = "X-Sign";

    /** nonce 防重放记录容器：key = nonce，value = 过期时间戳（毫秒） */
    private final ConcurrentHashMap<String, Long> nonceRecords = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 接口签名密钥，从配置文件读取 */
    @Value("${api.sign.secret:BuildFlowApiSignSecret2026}")
    private String secret;

    /** 签名有效期（毫秒），默认5分钟 */
    @Value("${api.sign.timeout:300000}")
    private long timeout;

    /**
     * 请求预处理，执行签名校验
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器对象
     * @return true放行，false拒绝请求
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 非Controller方法直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireSign requireSign = handlerMethod.getMethodAnnotation(RequireSign.class);

        // 无签名注解，放行
        if (requireSign == null) {
            return true;
        }

        // 获取签名参数
        String appKey = request.getHeader(HEADER_APP_KEY);
        String timestamp = request.getHeader(HEADER_TIMESTAMP);
        String nonce = request.getHeader(HEADER_NONCE);
        String sign = request.getHeader(HEADER_SIGN);

        // 参数完整性校验
        if (!StringUtils.hasText(appKey) || !StringUtils.hasText(timestamp)
                || !StringUtils.hasText(nonce) || !StringUtils.hasText(sign)) {
            log.warn("签名参数缺失，IP={}", getClientIp(request));
            writeErrorResponse(response, "签名参数缺失");
            return false;
        }

        // 时间戳有效期校验
        long requestTime;
        try {
            requestTime = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            writeErrorResponse(response, "时间戳格式错误");
            return false;
        }
        long now = System.currentTimeMillis();
        if (Math.abs(now - requestTime) > timeout) {
            log.warn("签名已过期，timestamp={}, now={}", requestTime, now);
            writeErrorResponse(response, "请求已过期");
            return false;
        }

        // nonce 防重放校验
        if (isNonceRepeated(nonce, requestTime + timeout)) {
            log.warn("nonce 重复，nonce={}", nonce);
            writeErrorResponse(response, "请勿重复请求");
            return false;
        }

        // 签名计算与校验
        String calculatedSign = calculateSign(appKey, timestamp, nonce);
        if (!calculatedSign.equals(sign)) {
            log.warn("签名校验失败，appKey={}, 请求sign={}, 计算sign={}", appKey, sign, calculatedSign);
            writeErrorResponse(response, "签名校验失败");
            return false;
        }

        log.debug("签名校验通过，appKey={}", appKey);
        return true;
    }

    /**
     * 计算请求签名
     * 算法：MD5(appkey + timestamp + nonce + secret)
     * @param appKey 应用密钥标识
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @return MD5签名字符串（32位小写十六进制）
     */
    private String calculateSign(String appKey, String timestamp, String nonce) {
        String raw = appKey + timestamp + nonce + secret;
        return DigestUtils.md5DigestAsHex(raw.getBytes());
    }

    /**
     * 检查 nonce 是否重复（防重放）
     * 若 nonce 已存在且未过期则判定为重复，否则记录该 nonce
     * @param nonce 随机数
     * @param expireTime 过期时间戳（毫秒）
     * @return true表示重复，false表示首次使用
     */
    private boolean isNonceRepeated(String nonce, long expireTime) {
        long now = System.currentTimeMillis();
        // 清理过期 nonce
        cleanExpiredNonces(now);

        // 使用 putIfAbsent 保证原子性
        Long existing = nonceRecords.putIfAbsent(nonce, expireTime);
        return existing != null && existing > now;
    }

    /**
     * 清理已过期的 nonce 记录
     * @param now 当前时间戳（毫秒）
     */
    private void cleanExpiredNonces(long now) {
        Iterator<Map.Entry<String, Long>> iterator = nonceRecords.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() <= now) {
                iterator.remove();
            }
        }
    }

    /**
     * 获取客户端IP地址
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 写入错误响应JSON
     * @param response HTTP响应
     * @param message 错误消息
     * @throws Exception 写入异常
     */
    private void writeErrorResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(R.fail(401, message)));
    }
}
