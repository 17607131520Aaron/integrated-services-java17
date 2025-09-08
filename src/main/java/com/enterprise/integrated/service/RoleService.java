package com.enterprise.integrated.service;

public interface RoleService {

    void assignRoleToUser(Long userId, Long roleId);

    void removeRoleFromUser(Long userId, Long roleId);

    void addPermissionToRole(Long roleId, Long permissionId);

    void removePermissionFromRole(Long roleId, Long permissionId);

    com.enterprise.integrated.dto.BatchOperationResult assignRoleToUsers(Long roleId, java.util.List<Long> userIds);

    com.enterprise.integrated.dto.BatchOperationResult removeRoleFromUsers(Long roleId, java.util.List<Long> userIds);

    com.enterprise.integrated.dto.BatchOperationResult addPermissionsToRole(Long roleId, java.util.List<Long> permissionIds);

    com.enterprise.integrated.dto.BatchOperationResult removePermissionsFromRole(Long roleId, java.util.List<Long> permissionIds);
}


