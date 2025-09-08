package com.enterprise.integrated.service;

import com.enterprise.integrated.mapper.RoleMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MenuCacheFacade {

    private final MenuCacheService menuCacheService;
    private final RoleMapper roleMapper;

    public MenuCacheFacade(MenuCacheService menuCacheService, RoleMapper roleMapper) {
        this.menuCacheService = menuCacheService;
        this.roleMapper = roleMapper;
    }

    /**
     * 在用户-角色关系发生变化后调用
     */
    public void onUserRolesChanged(Long userId) {
        if (userId == null) return;
        menuCacheService.evictVisibleMenuTreeForUser(userId);
    }

    /**
     * 在角色-权限关系发生变化后调用
     */
    public void onRolePermissionsChanged(Long roleId) {
        if (roleId == null) return;
        List<Long> userIds = roleMapper.findUserIdsByRoleId(roleId);
        if (userIds != null && !userIds.isEmpty()) {
            menuCacheService.evictVisibleMenuTreeForUsers(userIds);
        }
    }

    /**
     * 在菜单-权限发生结构性变化后调用（例如批量绑定解绑影响范围未知）
     */
    public void onMenusPermissionsMassChanged() {
        menuCacheService.evictAll("menuTree");
        menuCacheService.evictAll("visibleMenuTree");
    }
}


