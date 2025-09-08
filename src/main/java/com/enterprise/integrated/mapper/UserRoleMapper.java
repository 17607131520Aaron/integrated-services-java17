package com.enterprise.integrated.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRoleMapper {

    int addUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    int removeUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    int countUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    int addUserRolesBatch(@Param("userId") Long userId, @Param("roleIds") java.util.List<Long> roleIds);

    int removeUserRolesBatch(@Param("userId") Long userId, @Param("roleIds") java.util.List<Long> roleIds);

    /** 仅判断存在性，效率高于 COUNT */
    boolean existsUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}


