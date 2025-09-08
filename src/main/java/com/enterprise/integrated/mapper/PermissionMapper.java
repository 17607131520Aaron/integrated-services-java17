package com.enterprise.integrated.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.integrated.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    List<Permission> findPermissionsByUserId(@Param("userId") Long userId);

    List<Permission> findMenusByUserId(@Param("userId") Long userId);

    IPage<Permission> pagePermissionsByUserId(IPage<?> page, @Param("userId") Long userId);

    IPage<Permission> pageMenusByUserId(IPage<?> page, @Param("userId") Long userId);
}


