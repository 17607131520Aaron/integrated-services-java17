package com.enterprise.integrated.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RolePermissionMapper {

    @Insert("INSERT INTO sys_role_permission(role_id, permission_id) VALUES(#{roleId}, #{permissionId})")
    int addRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int removeRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    @Select("SELECT COUNT(1) FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int countRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}


