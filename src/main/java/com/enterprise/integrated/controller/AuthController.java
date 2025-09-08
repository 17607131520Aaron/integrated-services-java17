package com.enterprise.integrated.controller;

import com.enterprise.integrated.common.result.Result;
import com.enterprise.integrated.dto.LoginRequest;
import com.enterprise.integrated.dto.LoginResponse;
import com.enterprise.integrated.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return Result.success("登录成功", response);
    }

    @Operation(summary = "用户登出", description = "用户退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return Result.success();
    }

    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestParam("refreshToken") String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return Result.success("令牌刷新成功", response);
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public Result<LoginResponse> getCurrentUser() {
        LoginResponse response = authService.getCurrentUser();
        return Result.success(response);
    }
}
