package com.enterprise.integrated.aspect;

import com.enterprise.integrated.annotation.OperationLog;
import com.enterprise.integrated.security.UserDetailsServiceImpl;
import com.enterprise.integrated.service.OperationLogService;
import com.enterprise.integrated.utils.IpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志AOP切面
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    public OperationLogAspect(OperationLogService operationLogService, ObjectMapper objectMapper) {
        this.operationLogService = operationLogService;
        this.objectMapper = objectMapper;
    }

    /**
     * 定义切点
     */
    @Pointcut("@annotation(com.enterprise.integrated.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    /**
     * 环绕通知
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLogAnnotation = method.getAnnotation(OperationLog.class);
        
        // 获取请求信息
        HttpServletRequest request = getHttpServletRequest();
        
        // 创建操作日志对象
        com.enterprise.integrated.entity.OperationLog operationLog = new com.enterprise.integrated.entity.OperationLog();
        
        try {
            // 设置基本信息
            setBasicInfo(operationLog, operationLogAnnotation, request, joinPoint);
            
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 记录成功信息
            long executionTime = System.currentTimeMillis() - startTime;
            operationLog.setExecutionTime(executionTime);
            operationLog.setStatus(1);
            
            // 记录返回结果
            if (operationLogAnnotation.recordResult() && result != null) {
                try {
                    operationLog.setResponse(objectMapper.writeValueAsString(result));
                } catch (Exception e) {
                    operationLog.setResponse("序列化失败: " + e.getMessage());
                }
            }
            
            // 异步保存日志
            saveOperationLogAsync(operationLog);
            
            return result;
            
        } catch (Exception e) {
            // 记录失败信息
            long executionTime = System.currentTimeMillis() - startTime;
            operationLog.setExecutionTime(executionTime);
            operationLog.setStatus(0);
            operationLog.setErrorMessage(e.getMessage());
            
            // 异步保存日志
            saveOperationLogAsync(operationLog);
            
            throw e;
        }
    }

    /**
     * 设置基本信息
     */
    private void setBasicInfo(com.enterprise.integrated.entity.OperationLog operationLog, 
                            OperationLog annotation, 
                            HttpServletRequest request, 
                            ProceedingJoinPoint joinPoint) {
        
        // 设置用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsServiceImpl.CustomUserDetails) {
            UserDetailsServiceImpl.CustomUserDetails userDetails = 
                (UserDetailsServiceImpl.CustomUserDetails) authentication.getPrincipal();
            operationLog.setUserId(userDetails.getUserId());
            operationLog.setUsername(userDetails.getUsername());
        }
        
        // 设置操作信息
        operationLog.setOperation(annotation.value());
        operationLog.setOperationType(annotation.type().getDescription());
        
        if (request != null) {
            // 设置请求信息
            operationLog.setMethod(request.getMethod());
            operationLog.setRequestUrl(request.getRequestURL().toString());
            operationLog.setIpAddress(IpUtils.getClientIpAddress(request));
            operationLog.setUserAgent(request.getHeader("User-Agent"));
            
            // 记录请求参数
            if (annotation.recordParams()) {
                try {
                    Object[] args = joinPoint.getArgs();
                    if (args != null && args.length > 0) {
                        String json = objectMapper.writeValueAsString(args);
                        // 简单脱敏常见敏感字段
                        json = json.replaceAll("\\\"password\\\"\\s*:\\s*\\\".*?\\\"", "\"password\":\"******\"")
                                   .replaceAll("\\\"oldPassword\\\"\\s*:\\s*\\\".*?\\\"", "\"oldPassword\":\"******\"")
                                   .replaceAll("\\\"newPassword\\\"\\s*:\\s*\\\".*?\\\"", "\"newPassword\":\"******\"")
                                   .replaceAll("\\\"idCard\\\"\\s*:\\s*\\\"(\\w{4})\\w+(\\w{4})\\\"", "\"idCard\":\"$1********$2\"")
                                   .replaceAll("\\\"phone\\\"\\s*:\\s*\\\"(\\d{3})\\d{4}(\\d{4})\\\"", "\"phone\":\"$1****$2\"");
                        operationLog.setRequestParams(json);
                    }
                } catch (Exception e) {
                    operationLog.setRequestParams("参数序列化失败: " + e.getMessage());
                }
            }
        }
        
        operationLog.setCreateTime(LocalDateTime.now());
    }

    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 异步保存操作日志
     */
    private void saveOperationLogAsync(com.enterprise.integrated.entity.OperationLog operationLog) {
        try {
            operationLogService.saveAsync(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
}
