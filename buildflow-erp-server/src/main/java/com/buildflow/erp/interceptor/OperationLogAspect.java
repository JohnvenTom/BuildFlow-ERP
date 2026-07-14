package com.buildflow.erp.interceptor;

import com.buildflow.erp.common.utils.LogMaskUtil;
import com.buildflow.erp.entity.SysOperationLog;
import com.buildflow.erp.service.SysOperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志AOP切面
 * 拦截com.buildflow.erp.controller包下所有增删改方法，自动采集操作日志。
 * 从注解或方法名判断操作类型，从HttpServletRequest获取IP，从SecurityContext获取当前用户，
 * 异步保存日志到数据库。
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private SysOperationLogService sysOperationLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 定义切点：拦截controller包下所有方法
     * 匹配com.buildflow.erp.controller包及其子包下的所有类的所有方法
     */
    @Pointcut("execution(* com.buildflow.erp.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：采集操作日志
     * 仅对增删改方法（方法名以add/save/insert/delete/remove/update/edit/reset/assign开头）记录日志，
     * 查询方法不记录。从方法名推断操作类型，从请求中获取IP和参数，从安全上下文获取当前用户。
     *
     * @param joinPoint 切面连接点，包含目标方法信息
     * @return 目标方法的执行结果
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 执行目标方法
        Object result = joinPoint.proceed();

        // 获取方法名，判断是否为增删改操作
        String methodName = joinPoint.getSignature().getName();
        if (!isModifyMethod(methodName)) {
            return result;
        }

        try {
            // 构建操作日志
            SysOperationLog log = new SysOperationLog();

            // 从SecurityContext获取当前用户ID（JwtAuthenticationFilter设置的）
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() != null) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof Long) {
                    log.setUserId((Long) principal);
                }
            }

            // 从HttpServletRequest获取用户名
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIp(getIpAddress(request));
                // 从request属性获取用户名（JwtAuthenticationFilter设置的）
                Object usernameAttr = request.getAttribute("username");
                if (usernameAttr != null) {
                    log.setUsername(usernameAttr.toString());
                }
                if (log.getUserId() == null) {
                    Object userIdAttr = request.getAttribute("userId");
                    if (userIdAttr != null) {
                        log.setUserId(Long.valueOf(userIdAttr.toString()));
                    }
                }
                String className = joinPoint.getTarget().getClass().getSimpleName();
                log.setMethod(className + "#" + methodName);
            }

            // 设置操作模块（从类名提取）
            String className = joinPoint.getTarget().getClass().getSimpleName();
            log.setModule(extractModule(className));

            // 设置操作描述
            log.setOperation(extractOperation(methodName));

            // 设置请求参数
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    String params = objectMapper.writeValueAsString(args);
                    // 限制参数长度，避免超长数据写入
                    if (params.length() > 2000) {
                        params = params.substring(0, 2000);
                    }
                    // 敏感信息脱敏处理
                    params = LogMaskUtil.maskJson(params);
                    log.setParams(params);
                } catch (Exception e) {
                    log.setParams("参数序列化失败");
                }
            }

            // 异步保存日志
            sysOperationLogService.save(log);
        } catch (Exception e) {
            // 日志记录失败不影响业务执行
        }

        return result;
    }

    /**
     * 判断是否为增删改方法
     * 根据方法名前缀判断操作类型，查询方法返回false
     *
     * @param methodName 方法名称
     * @return 是增删改方法返回true，否则返回false
     */
    private boolean isModifyMethod(String methodName) {
        if (methodName == null) {
            return false;
        }
        String lower = methodName.toLowerCase();
        return lower.startsWith("add") || lower.startsWith("save") || lower.startsWith("insert")
                || lower.startsWith("delete") || lower.startsWith("remove")
                || lower.startsWith("update") || lower.startsWith("edit")
                || lower.startsWith("reset") || lower.startsWith("assign");
    }

    /**
     * 从类名提取模块名称
     * 移除Controller后缀作为模块名称，如SysUserController -> SysUser
     *
     * @paramClassName 类名称
     * @return 模块名称
     */
    private String extractModule(String className) {
        if (className == null) {
            return "未知模块";
        }
        if (className.endsWith("Controller")) {
            return className.substring(0, className.length() - "Controller".length());
        }
        return className;
    }

    /**
     * 从方法名提取操作描述
     * 根据方法名前缀映射为中文操作描述
     *
     * @param methodName 方法名称
     * @return 操作描述
     */
    private String extractOperation(String methodName) {
        if (methodName == null) {
            return "未知操作";
        }
        String lower = methodName.toLowerCase();
        if (lower.startsWith("add") || lower.startsWith("save") || lower.startsWith("insert")) {
            return "新增";
        } else if (lower.startsWith("delete") || lower.startsWith("remove")) {
            return "删除";
        } else if (lower.startsWith("update") || lower.startsWith("edit")) {
            return "修改";
        } else if (lower.startsWith("reset")) {
            return "重置";
        } else if (lower.startsWith("assign")) {
            return "分配";
        }
        return "操作";
    }

    /**
     * 获取客户端真实IP地址
     * 依次从X-Forwarded-For、X-Real-IP、Proxy-Client-IP等请求头中获取，
     * 均无法获取时使用RemoteAddr
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
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
        // X-Forwarded-For可能包含多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
