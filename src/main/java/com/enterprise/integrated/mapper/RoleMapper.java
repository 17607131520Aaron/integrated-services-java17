package com.enterprise.integrated.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.integrated.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    List<Role> findRolesByUserId(@Param("userId") Long userId);

    List<Long> findUserIdsByRoleId(@Param("roleId") Long roleId);

    IPage<Role> pageRolesByUserId(IPage<?> page, @Param("userId") Long userId);
}


