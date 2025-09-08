package com.enterprise.integrated.controller;

import com.enterprise.integrated.annotation.OperationLog;
import com.enterprise.integrated.annotation.RateLimit;
import com.enterprise.integrated.dto.LoginRequest;
import com.enterprise.integrated.dto.LoginResponse;
import com.enterprise.integrated.dto.UserDTO;
import com.enterprise.integrated.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "用户登录", description = "用户名密码登录")
    @PostMapping("/login")
    @OperationLog(value = "用户登录", type = OperationLog.OperationType.LOGIN)
    @RateLimit(key = "login", count = 5, time = 60, limitType = RateLimit.LimitType.IP, message = "登录过于频繁，请稍后再试")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    @PreAuthorize("isAuthenticated()")
    @OperationLog(value = "用户登出", type = OperationLog.OperationType.LOGOUT)
    public void logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
    }

    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    @RateLimit(key = "refresh", count = 10, time = 60, limitType = RateLimit.LimitType.IP)
    public LoginResponse refresh(@RequestParam("refreshToken") String refreshToken) {
        return authService.refreshToken(refreshToken);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    @PreAuthorize("isAuthenticated()")
    @RateLimit(key = "current", count = 20, time = 60, limitType = RateLimit.LimitType.USER)
    public UserDTO getCurrentUser() {
        return authService.getCurrentUser();
    }
}
