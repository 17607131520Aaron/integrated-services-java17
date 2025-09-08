package com.enterprise.integrated.service;

import com.enterprise.integrated.dto.LoginRequest;
import com.enterprise.integrated.dto.LoginResponse;
import com.enterprise.integrated.dto.UserDTO;

/**
 * 认证服务接口
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
public interface AuthService {

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户登出
     * 
     * @param token 访问令牌
     */
    void logout(String token);

    /**
     * 刷新令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的登录响应
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 获取当前用户信息
     * 
     * @return 当前用户信息
     */
    UserDTO getCurrentUser();

    /**
     * 验证令牌
     * 
     * @param token 访问令牌
     * @return 用户ID
     */
    Long validateToken(String token);

    /**
     * 生成访问令牌
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return 访问令牌
     */
    String generateAccessToken(Long userId, String username);

    /**
     * 生成刷新令牌
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return 刷新令牌
     */
    String generateRefreshToken(Long userId, String username);
}
