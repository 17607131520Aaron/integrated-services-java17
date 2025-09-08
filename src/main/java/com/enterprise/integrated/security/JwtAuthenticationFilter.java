package com.enterprise.integrated.security;

import com.enterprise.integrated.service.AuthService;
import com.enterprise.integrated.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import io.jsonwebtoken.Claims;

/**
 * JWT认证过滤器
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final AuthService authService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, 
                                 UserDetailsService userDetailsService,
                                 AuthService authService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 验证令牌
            Long userId = authService.validateToken(token);
            if (userId != null) {
                // 校验令牌类型：仅允许access令牌
                try {
                    Claims claims = (Claims) jwtUtils.getClass().getDeclaredMethod("getClaimsFromToken", String.class)
                            .invoke(jwtUtils, token);
                    Object type = claims.get("type");
                    if (type == null || !"access".equals(String.valueOf(type))) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                } catch (Exception ignored) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String username = jwtUtils.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
