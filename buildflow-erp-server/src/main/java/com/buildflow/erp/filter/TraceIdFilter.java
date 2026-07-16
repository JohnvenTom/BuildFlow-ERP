package com.buildflow.erp.filter;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * TraceId链路追踪过滤器
 * 为每个HTTP请求生成/透传唯一traceId，写入MDC（Mapped Diagnostic Context），
 * 使同一请求在整条调用链路上的日志都能通过traceId串联。
 *
 * <p>处理流程：
 * <ul>
 *   <li>优先从请求头 X-Trace-Id 获取上游传入的traceId</li>
 *   <li>若无则生成UUID（去掉横线，取前16位）作为新的traceId</li>
 *   <li>放入MDC，key = "traceId"，供日志pattern中 %X{traceId} 输出</li>
 *   <li>设置到响应头 X-Trace-Id 返回给前端，便于排查问题</li>
 *   <li>在finally中清理MDC，避免线程复用导致traceId串用</li>
 * </ul></p>
 */
public class TraceIdFilter extends OncePerRequestFilter {

    /** traceId请求头名称 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** MDC中traceId的key */
    public static final String TRACE_ID_KEY = "traceId";

    /** 生成的traceId长度（UUID去横线后取前16位） */
    private static final int TRACE_ID_LENGTH = 16;

    /**
     * 过滤器核心逻辑：生成/透传traceId并写入MDC
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 优先从请求头获取上游传入的traceId
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            // 无上游traceId则生成新的：UUID去横线取前16位
            traceId = generateTraceId();
        }
        try {
            // 放入MDC，供日志输出
            MDC.put(TRACE_ID_KEY, traceId);
            // 设置到响应头返回给前端
            response.setHeader(TRACE_ID_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            // 清理MDC，避免线程池复用导致traceId串用
            MDC.remove(TRACE_ID_KEY);
        }
    }

    /**
     * 生成traceId：UUID去掉横线后取前16位
     * @return 16位长度的traceId字符串
     */
    private String generateTraceId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid.substring(0, TRACE_ID_LENGTH);
    }
}
