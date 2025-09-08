package com.enterprise.integrated.service.impl;

import com.enterprise.integrated.mapper.RolePermissionMapper;
import com.enterprise.integrated.mapper.UserRoleMapper;
import com.enterprise.integrated.mapper.UserMapper;
import com.enterprise.integrated.mapper.RoleMapper;
import com.enterprise.integrated.mapper.PermissionMapper;
import com.enterprise.integrated.entity.User;
import com.enterprise.integrated.entity.Role;
import com.enterprise.integrated.entity.Permission;
import com.enterprise.integrated.service.MenuCacheFacade;
import com.enterprise.integrated.service.RoleService;
import com.enterprise.integrated.common.exception.BusinessException;
import com.enterprise.integrated.common.result.ResultCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleServiceImpl implements RoleService {

    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final MenuCacheFacade menuCacheFacade;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public RoleServiceImpl(UserRoleMapper userRoleMapper,
                           RolePermissionMapper rolePermissionMapper,
                           MenuCacheFacade menuCacheFacade,
                           UserMapper userMapper,
                           RoleMapper roleMapper,
                           PermissionMapper permissionMapper) {
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.menuCacheFacade = menuCacheFacade;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleToUser(Long userId, Long roleId) {
        // 基础存在性与可用性校验
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() != 0 || user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在或已禁用");
        }
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() != 0 || role.getStatus() == null || role.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "角色不存在或已禁用");
        }
        // 幂等校验
        if (userRoleMapper.countUserRole(userId, roleId) > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "用户已拥有该角色");
        }
        int rows = userRoleMapper.addUserRole(userId, roleId);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "分配角色失败");
        }
        menuCacheFacade.onUserRolesChanged(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRoleFromUser(Long userId, Long roleId) {
        if (userRoleMapper.countUserRole(userId, roleId) == 0) {
            return; // 已无该关系，视为成功
        }
        int rows = userRoleMapper.removeUserRole(userId, roleId);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "移除角色失败");
        }
        menuCacheFacade.onUserRolesChanged(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPermissionToRole(Long roleId, Long permissionId) {
        // 基础存在性与可用性校验
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() != 0 || role.getStatus() == null || role.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "角色不存在或已禁用");
        }
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null || permission.getDeleted() != 0 || permission.getStatus() == null || permission.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "权限不存在或已禁用");
        }
        if (rolePermissionMapper.countRolePermission(roleId, permissionId) > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "角色已拥有该权限");
        }
        int rows = rolePermissionMapper.addRolePermission(roleId, permissionId);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "绑定权限失败");
        }
        menuCacheFacade.onRolePermissionsChanged(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        if (rolePermissionMapper.countRolePermission(roleId, permissionId) == 0) {
            return; // 已无该关系，视为成功
        }
        int rows = rolePermissionMapper.removeRolePermission(roleId, permissionId);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "移除权限失败");
        }
        menuCacheFacade.onRolePermissionsChanged(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public com.enterprise.integrated.dto.BatchOperationResult assignRoleToUsers(Long roleId, java.util.List<Long> userIds) {
        com.enterprise.integrated.dto.BatchOperationResult result = new com.enterprise.integrated.dto.BatchOperationResult();
        if (userIds == null || userIds.isEmpty()) return result;
        result.setTotalCount(userIds.size());
        // 角色可用性校验
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() != 0 || role.getStatus() == null || role.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "角色不存在或已禁用");
        }
        for (Long userId : userIds) {
            User u = userMapper.selectById(userId);
            if (u == null || u.getDeleted() != 0 || u.getStatus() == null || u.getStatus() == 0) {
                result.getInvalidIds().add(userId);
                result.setSkippedCount(result.getSkippedCount() + 1);
                continue;
            }
            if (userRoleMapper.countUserRole(userId, roleId) == 0) {
                userRoleMapper.addUserRole(userId, roleId);
                menuCacheFacade.onUserRolesChanged(userId);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } else {
                result.getDuplicatedIds().add(userId);
                result.setSkippedCount(result.getSkippedCount() + 1);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public com.enterprise.integrated.dto.BatchOperationResult removeRoleFromUsers(Long roleId, java.util.List<Long> userIds) {
        com.enterprise.integrated.dto.BatchOperationResult result = new com.enterprise.integrated.dto.BatchOperationResult();
        if (userIds == null || userIds.isEmpty()) return result;
        result.setTotalCount(userIds.size());
        for (Long userId : userIds) {
            if (userRoleMapper.countUserRole(userId, roleId) > 0) {
                userRoleMapper.removeUserRole(userId, roleId);
                menuCacheFacade.onUserRolesChanged(userId);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } else {
                result.getDuplicatedIds().add(userId);
                result.setSkippedCount(result.getSkippedCount() + 1);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public com.enterprise.integrated.dto.BatchOperationResult addPermissionsToRole(Long roleId, java.util.List<Long> permissionIds) {
        com.enterprise.integrated.dto.BatchOperationResult result = new com.enterprise.integrated.dto.BatchOperationResult();
        if (permissionIds == null || permissionIds.isEmpty()) return result;
        result.setTotalCount(permissionIds.size());
        // 角色可用性校验
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() != 0 || role.getStatus() == null || role.getStatus() == 0) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "角色不存在或已禁用");
        }
        boolean changed = false;
        for (Long pid : permissionIds) {
            Permission p = permissionMapper.selectById(pid);
            if (p == null || p.getDeleted() != 0 || p.getStatus() == null || p.getStatus() == 0) {
                result.getInvalidIds().add(pid);
                result.setSkippedCount(result.getSkippedCount() + 1);
                continue;
            }
            if (rolePermissionMapper.countRolePermission(roleId, pid) == 0) {
                rolePermissionMapper.addRolePermission(roleId, pid);
                changed = true;
                result.setSuccessCount(result.getSuccessCount() + 1);
            } else {
                result.getDuplicatedIds().add(pid);
                result.setSkippedCount(result.getSkippedCount() + 1);
            }
        }
        if (changed) {
            menuCacheFacade.onRolePermissionsChanged(roleId);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public com.enterprise.integrated.dto.BatchOperationResult removePermissionsFromRole(Long roleId, java.util.List<Long> permissionIds) {
        com.enterprise.integrated.dto.BatchOperationResult result = new com.enterprise.integrated.dto.BatchOperationResult();
        if (permissionIds == null || permissionIds.isEmpty()) return result;
        result.setTotalCount(permissionIds.size());
        boolean changed = false;
        for (Long pid : permissionIds) {
            if (rolePermissionMapper.countRolePermission(roleId, pid) > 0) {
                rolePermissionMapper.removeRolePermission(roleId, pid);
                changed = true;
                result.setSuccessCount(result.getSuccessCount() + 1);
            } else {
                result.getDuplicatedIds().add(pid);
                result.setSkippedCount(result.getSkippedCount() + 1);
            }
        }
        if (changed) {
            menuCacheFacade.onRolePermissionsChanged(roleId);
        }
        return result;
    }
}


