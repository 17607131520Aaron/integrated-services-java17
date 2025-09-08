package com.enterprise.integrated.controller;

import com.enterprise.integrated.annotation.OperationLog;
import com.enterprise.integrated.service.RoleService;
import com.enterprise.integrated.dto.BatchAssignUsersRequest;
import com.enterprise.integrated.dto.BatchBindPermissionsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "角色管理", description = "用户角色与角色权限管理")
@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "分配角色给用户")
    @PostMapping("/{roleId}/users/{userId}")
    @PreAuthorize("hasAuthority('perm:system:role:assign') or hasRole('ADMIN')")
    @OperationLog(value = "分配用户角色", type = OperationLog.OperationType.INSERT)
    public void assignRoleToUser(@PathVariable @NotNull Long roleId, @PathVariable @NotNull Long userId) {
        roleService.assignRoleToUser(userId, roleId);
    }

    @Operation(summary = "回收用户的某个角色")
    @DeleteMapping("/{roleId}/users/{userId}")
    @PreAuthorize("hasAuthority('perm:system:role:revoke') or hasRole('ADMIN')")
    @OperationLog(value = "回收用户角色", type = OperationLog.OperationType.DELETE)
    public void removeRoleFromUser(@PathVariable @NotNull Long roleId, @PathVariable @NotNull Long userId) {
        roleService.removeRoleFromUser(userId, roleId);
    }

    @Operation(summary = "给角色绑定权限")
    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('perm:system:role:perm:add') or hasRole('ADMIN')")
    @OperationLog(value = "角色绑定权限", type = OperationLog.OperationType.INSERT)
    public void addPermissionToRole(@PathVariable @NotNull Long roleId, @PathVariable @NotNull Long permissionId) {
        roleService.addPermissionToRole(roleId, permissionId);
    }

    @Operation(summary = "移除角色绑定的权限")
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('perm:system:role:perm:remove') or hasRole('ADMIN')")
    @OperationLog(value = "移除角色权限", type = OperationLog.OperationType.DELETE)
    public void removePermissionFromRole(@PathVariable @NotNull Long roleId, @PathVariable @NotNull Long permissionId) {
        roleService.removePermissionFromRole(roleId, permissionId);
    }

    @Operation(summary = "批量为用户分配角色")
    @PostMapping("/assign/batch")
    @PreAuthorize("hasAuthority('perm:system:role:assign') or hasRole('ADMIN')")
    @OperationLog(value = "批量分配用户角色", type = OperationLog.OperationType.INSERT)
    public com.enterprise.integrated.dto.BatchOperationResult assignRoleToUsers(@RequestBody @jakarta.validation.Valid BatchAssignUsersRequest req) {
        return roleService.assignRoleToUsers(req.getRoleId(), req.getUserIds());
    }

    @Operation(summary = "批量回收用户角色")
    @DeleteMapping("/revoke/batch")
    @PreAuthorize("hasAuthority('perm:system:role:revoke') or hasRole('ADMIN')")
    @OperationLog(value = "批量回收用户角色", type = OperationLog.OperationType.DELETE)
    public com.enterprise.integrated.dto.BatchOperationResult removeRoleFromUsers(@RequestBody @jakarta.validation.Valid BatchAssignUsersRequest req) {
        return roleService.removeRoleFromUsers(req.getRoleId(), req.getUserIds());
    }

    @Operation(summary = "批量为角色绑定权限")
    @PostMapping("/permissions/batch")
    @PreAuthorize("hasAuthority('perm:system:role:perm:add') or hasRole('ADMIN')")
    @OperationLog(value = "批量绑定角色权限", type = OperationLog.OperationType.INSERT)
    public com.enterprise.integrated.dto.BatchOperationResult addPermissionsToRole(@RequestBody @jakarta.validation.Valid BatchBindPermissionsRequest req) {
        return roleService.addPermissionsToRole(req.getRoleId(), req.getPermissionIds());
    }

    @Operation(summary = "批量移除角色权限")
    @DeleteMapping("/permissions/batch")
    @PreAuthorize("hasAuthority('perm:system:role:perm:remove') or hasRole('ADMIN')")
    @OperationLog(value = "批量移除角色权限", type = OperationLog.OperationType.DELETE)
    public com.enterprise.integrated.dto.BatchOperationResult removePermissionsFromRole(@RequestBody @jakarta.validation.Valid BatchBindPermissionsRequest req) {
        return roleService.removePermissionsFromRole(req.getRoleId(), req.getPermissionIds());
    }
}


