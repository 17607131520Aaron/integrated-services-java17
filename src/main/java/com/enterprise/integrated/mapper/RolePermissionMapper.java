package com.enterprise.integrated.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RolePermissionMapper {

    int addRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    int removeRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    int countRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    int addRolePermissionsBatch(@Param("roleId") Long roleId, @Param("permissionIds") java.util.List<Long> permissionIds);

    int removeRolePermissionsBatch(@Param("roleId") Long roleId, @Param("permissionIds") java.util.List<Long> permissionIds);

    /** 仅判断存在性，效率高于 COUNT */
    boolean existsRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}


