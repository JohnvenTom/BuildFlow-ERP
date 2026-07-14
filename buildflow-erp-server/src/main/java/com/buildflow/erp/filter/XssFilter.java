package com.buildflow.erp.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * XSS过滤器
 * 继承 OncePerRequestFilter，对请求参数进行HTML转义，防止跨站脚本攻击
 *
 * <p>处理范围：
 * <ul>
 *   <li>仅对 Content-Type 为 application/x-www-form-urlencoded 的请求处理</li>
 *   <li>将请求参数中的 &lt; &gt; &quot; &#39; &amp; 等特殊字符转义</li>
 *   <li>使用 XssHttpServletRequestWrapper 包装原始请求</li>
 * </ul></p>
 *
 * <p>注意：JSON请求体（application/json）不在此过滤器处理范围，JSON反序列化时
 * 框架会自行处理编码，业务层需对富文本内容单独做转义。</p>
 *
 * @see XssHttpServletRequestWrapper
 */
public class XssFilter extends OncePerRequestFilter {

    /** 表单类型的Content-Type */
    private static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    /**
     * 过滤器核心逻辑
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
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains(CONTENT_TYPE_FORM)) {
            // 包装请求，对参数进行HTML转义
            XssHttpServletRequestWrapper wrappedRequest = new XssHttpServletRequestWrapper(request);
            filterChain.doFilter(wrappedRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
