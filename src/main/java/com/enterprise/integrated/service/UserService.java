package com.enterprise.integrated.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.integrated.dto.UserDTO;
import com.enterprise.integrated.entity.User;

/**
 * 用户服务接口
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    User findByEmail(String email);

    /**
     * 根据手机号查询用户
     * 
     * @param phone 手机号
     * @return 用户信息
     */
    User findByPhone(String phone);

    /**
     * 创建用户
     * 
     * @param userDTO 用户信息
     * @return 创建的用户
     */
    User createUser(UserDTO userDTO);

    /**
     * 更新用户信息
     * 
     * @param id 用户ID
     * @param userDTO 用户信息
     * @return 更新的用户
     */
    User updateUser(Long id, UserDTO userDTO);

    /**
     * 分页查询用户
     * 
     * @param page 分页参数
     * @param username 用户名(可选)
     * @param email 邮箱(可选)
     * @param status 状态(可选)
     * @return 分页结果
     */
    IPage<User> pageUsers(Page<User> page, String username, String email, Integer status);

    /**
     * 启用/禁用用户
     * 
     * @param id 用户ID
     * @param status 状态(0:禁用,1:启用)
     * @return 是否成功
     */
    boolean updateUserStatus(Long id, Integer status);

    /**
     * 重置用户密码
     * 
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long id, String newPassword);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);
}
