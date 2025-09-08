package com.enterprise.integrated.service.impl;

import com.enterprise.integrated.common.exception.BusinessException;
import com.enterprise.integrated.common.result.ResultCode;
import com.enterprise.integrated.dto.LoginRequest;
import com.enterprise.integrated.dto.LoginResponse;
import com.enterprise.integrated.dto.UserDTO;
import com.enterprise.integrated.entity.User;
import com.enterprise.integrated.security.UserDetailsServiceImpl;
import com.enterprise.integrated.service.AuthService;
import com.enterprise.integrated.service.UserService;
import com.enterprise.integrated.utils.JwtUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:token:";

    public AuthServiceImpl(UserService userService, 
                          PasswordEncoder passwordEncoder, 
                          JwtUtils jwtUtils, 
                          RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 查找用户
        User user = userService.findByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "用户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 生成令牌
        String accessToken = generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = generateRefreshToken(user.getId(), user.getUsername());

        // 存储刷新令牌到Redis
        redisTemplate.opsForValue().set(
            REFRESH_TOKEN_PREFIX + user.getId(), 
            refreshToken, 
            7, 
            TimeUnit.DAYS
        );

        // 构建用户信息
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        return new LoginResponse(accessToken, refreshToken, jwtUtils.getExpiration() / 1000, userDTO);
    }

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            // 验证令牌并获取用户ID
            Long userId = validateToken(token);
            if (userId != null) {
                // 将令牌加入黑名单
                long remainingSeconds = jwtUtils.getRemainingSeconds(token);
                if (remainingSeconds > 0) {
                    redisTemplate.opsForValue().set(
                        TOKEN_BLACKLIST_PREFIX + token, 
                        "blacklisted", 
                        remainingSeconds, 
                        TimeUnit.SECONDS
                    );
                }

                // 删除刷新令牌
                redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
            }
        }
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 验证刷新令牌
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED, "刷新令牌无效或已过期");
        }

        // 从令牌中获取用户信息
        String username = jwtUtils.getUsernameFromToken(refreshToken);
        Long userId = jwtUtils.getUserIdFromToken(refreshToken);

        // 检查Redis中的刷新令牌
        String storedRefreshToken = (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "刷新令牌无效");
        }

        // 获取用户信息
        User user = userService.getById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在或已被禁用");
        }

        // 生成新的令牌
        String newAccessToken = generateAccessToken(userId, username);
        String newRefreshToken = generateRefreshToken(userId, username);

        // 更新Redis中的刷新令牌
        redisTemplate.opsForValue().set(
            REFRESH_TOKEN_PREFIX + userId, 
            newRefreshToken, 
            7, 
            TimeUnit.DAYS
        );

        // 构建用户信息
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        return new LoginResponse(newAccessToken, newRefreshToken, jwtUtils.getExpiration() / 1000, userDTO);
    }

    @Override
    public UserDTO getCurrentUser() {
        // 从Security上下文获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        UserDetailsServiceImpl.CustomUserDetails userDetails = 
            (UserDetailsServiceImpl.CustomUserDetails) authentication.getPrincipal();
        
        User user = userDetails.getUser();
        
        // 构建用户信息
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        return userDTO;
    }

    @Override
    public Long validateToken(String token) {
        try {
            // 检查令牌是否在黑名单中
            if (Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token))) {
                return null;
            }

            // 验证令牌
            if (jwtUtils.validateToken(token)) {
                // 仅允许access令牌
                String type = jwtUtils.getTokenType(token);
                if (!"access".equals(type)) {
                    return null;
                }
                return jwtUtils.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            // 令牌无效
        }
        return null;
    }

    @Override
    public String generateAccessToken(Long userId, String username) {
        return jwtUtils.generateToken(userId, username);
    }

    @Override
    public String generateRefreshToken(Long userId, String username) {
        return jwtUtils.generateRefreshToken(userId, username);
    }
}
