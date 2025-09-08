package com.enterprise.integrated.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.integrated.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问层
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    User findByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     * 
     * @param phone 手机号
     * @return 用户信息
     */
    User findByPhone(@Param("phone") String phone);
}
