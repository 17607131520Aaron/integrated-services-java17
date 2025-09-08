package com.enterprise.integrated.interceptor;

import com.enterprise.integrated.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 请求响应日志拦截器
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Component
public class RequestResponseLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingInterceptor.class);
    private static final String REQUEST_START_TIME = "REQUEST_START_TIME";
    private static final String TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 生成追踪ID
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(TRACE_ID, traceId);
        
        // 记录请求开始时间
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
        
        // 记录请求信息
        String clientIp = IpUtils.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        
        StringBuilder logMsg = new StringBuilder();
        logMsg.append("请求开始 - ");
        logMsg.append("Method: ").append(method).append(", ");
        logMsg.append("URI: ").append(uri);
        if (queryString != null) {
            logMsg.append("?").append(queryString);
        }
        logMsg.append(", IP: ").append(clientIp);
        logMsg.append(", UserAgent: ").append(userAgent);
        logMsg.append(", TraceId: ").append(traceId);
        
        log.info(logMsg.toString());
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) {
        try {
            // 计算请求耗时
            Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
            
            // 记录响应信息
            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();
            String traceId = MDC.get(TRACE_ID);
            
            StringBuilder logMsg = new StringBuilder();
            logMsg.append("请求结束 - ");
            logMsg.append("Method: ").append(method).append(", ");
            logMsg.append("URI: ").append(uri).append(", ");
            logMsg.append("Status: ").append(status).append(", ");
            logMsg.append("Duration: ").append(duration).append("ms");
            logMsg.append(", TraceId: ").append(traceId);
            
            if (ex != null) {
                logMsg.append(", Exception: ").append(ex.getClass().getSimpleName())
                      .append(" - ").append(ex.getMessage());
                log.error(logMsg.toString());
            } else {
                log.info(logMsg.toString());
            }
            
        } finally {
            // 清理MDC
            MDC.clear();
        }
    }
}
