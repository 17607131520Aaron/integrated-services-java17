package com.enterprise.integrated.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 安全头过滤器
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Component
@Order(1)
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // X-Content-Type-Options: 防止MIME类型嗅探
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        
        // X-Frame-Options: 防止点击劫持
        httpResponse.setHeader("X-Frame-Options", "DENY");
        
        // X-XSS-Protection: XSS保护
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Strict-Transport-Security: 强制HTTPS
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        
        // Content-Security-Policy: 内容安全策略
        httpResponse.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self' https:; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none'");
        
        // Referrer-Policy: 引用策略
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions-Policy: 权限策略
        httpResponse.setHeader("Permissions-Policy", 
            "camera=(), " +
            "microphone=(), " +
            "geolocation=(), " +
            "payment=(), " +
            "usb=()");
        
        // Cache-Control: 缓存控制
        if (request instanceof jakarta.servlet.http.HttpServletRequest) {
            jakarta.servlet.http.HttpServletRequest httpRequest = (jakarta.servlet.http.HttpServletRequest) request;
            if (httpRequest.getServletPath().startsWith("/api/")) {
                httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                httpResponse.setHeader("Pragma", "no-cache");
                httpResponse.setHeader("Expires", "0");
            }
        }
        
        chain.doFilter(request, response);
    }
}
