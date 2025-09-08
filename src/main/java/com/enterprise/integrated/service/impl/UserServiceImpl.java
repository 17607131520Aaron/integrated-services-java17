package com.enterprise.integrated.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.integrated.common.exception.BusinessException;
import com.enterprise.integrated.common.result.ResultCode;
import com.enterprise.integrated.dto.UserDTO;
import com.enterprise.integrated.entity.User;
import com.enterprise.integrated.mapper.UserMapper;
import com.enterprise.integrated.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现类
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User findByUsername(String username) {
        return baseMapper.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return baseMapper.findByEmail(email);
    }

    @Override
    public User findByPhone(String phone) {
        return baseMapper.findByPhone(phone);
    }

    @Override
    public User createUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (existsByUsername(userDTO.getUsername())) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(userDTO.getEmail()) && existsByEmail(userDTO.getEmail())) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "邮箱已存在");
        }

        // 检查手机号是否已存在
        if (StringUtils.hasText(userDTO.getPhone()) && existsByPhone(userDTO.getPhone())) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "手机号已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        
        // 设置默认密码并加密
        String defaultPassword = "123456";
        user.setPassword(passwordEncoder.encode(defaultPassword));
        
        // 设置默认状态为启用
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        if (!save(user)) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "创建用户失败");
        }

        return user;
    }

    @Override
    public User updateUser(Long id, UserDTO userDTO) {
        User existingUser = getById(id);
        if (existingUser == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 检查用户名是否已被其他用户使用
        if (!existingUser.getUsername().equals(userDTO.getUsername()) && existsByUsername(userDTO.getUsername())) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "用户名已存在");
        }

        // 检查邮箱是否已被其他用户使用
        if (StringUtils.hasText(userDTO.getEmail()) && 
            !userDTO.getEmail().equals(existingUser.getEmail()) && 
            existsByEmail(userDTO.getEmail())) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "邮箱已存在");
        }

        // 检查手机号是否已被其他用户使用
        if (StringUtils.hasText(userDTO.getPhone()) && 
            !userDTO.getPhone().equals(existingUser.getPhone()) && 
            existsByPhone(userDTO.getPhone())) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "手机号已存在");
        }

        // 复制属性，排除密码
        BeanUtils.copyProperties(userDTO, existingUser, "password");

        if (!updateById(existingUser)) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "更新用户失败");
        }

        return existingUser;
    }

    @Override
    public IPage<User> pageUsers(Page<User> page, String username, String email, Integer status) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(username), User::getUsername, username)
                   .like(StringUtils.hasText(email), User::getEmail, email)
                   .eq(status != null, User::getStatus, status)
                   .orderByDesc(User::getCreateTime);

        return page(page, queryWrapper);
    }

    @Override
    public boolean updateUserStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        user.setStatus(status);
        return updateById(user);
    }

    @Override
    public boolean resetPassword(Long id, String newPassword) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }

    @Override
    public boolean existsByPhone(String phone) {
        return findByPhone(phone) != null;
    }
}
