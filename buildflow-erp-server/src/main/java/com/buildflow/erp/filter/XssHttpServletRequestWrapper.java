package com.buildflow.erp.filter;

import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * XSS请求包装器
 * 继承 HttpServletRequestWrapper，重写参数获取方法，对值进行HTML转义，防止XSS攻击
 *
 * <p>主要转义的字符包括：
 * <ul>
 *   <li>&lt; → &amp;lt;</li>
 *   <li>&gt; → &amp;gt;</li>
 *   <li>&quot; → &amp;quot;</li>
 *   <li>&#39; → &amp;#39;</li>
 *   <li>&amp; → &amp;amp;</li>
 * </ul></p>
 *
 * @see XssFilter
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 构造XSS请求包装器
     * @param request 原始HTTP请求对象
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * 获取单个参数值并转义
     * @param name 参数名
     * @return 转义后的参数值，无该参数返回null
     */
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return escape(value);
    }

    /**
     * 获取参数值数组并转义
     * @param name 参数名
     * @return 转义后的参数值数组，无该参数返回null
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] escapedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            escapedValues[i] = escape(values[i]);
        }
        return escapedValues;
    }

    /**
     * 获取所有参数Map并转义
     * @return 转义后的参数Map
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> originalMap = super.getParameterMap();
        Map<String, String[]> escapedMap = new LinkedHashMap<>(originalMap.size());
        for (Map.Entry<String, String[]> entry : originalMap.entrySet()) {
            String[] values = entry.getValue();
            String[] escapedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                escapedValues[i] = escape(values[i]);
            }
            escapedMap.put(entry.getKey(), escapedValues);
        }
        return escapedMap;
    }

    /**
     * 获取请求头值并转义
     * @param name 请求头名称
     * @return 转义后的请求头值
     */
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return escape(value);
    }

    /**
     * 对字符串进行HTML转义，防止XSS攻击
     * @param value 原始字符串
     * @return 转义后的字符串，输入为null或空串返回原值
     */
    private String escape(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return HtmlUtils.htmlEscape(value);
    }
}
