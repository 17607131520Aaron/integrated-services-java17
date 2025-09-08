package com.enterprise.integrated.service;

import com.enterprise.integrated.dto.MenuCreateRequest;
import com.enterprise.integrated.entity.Permission;
import com.enterprise.integrated.dto.MenuNode;
import com.enterprise.integrated.dto.MenuSortItem;
import java.util.List;

public interface PermissionService {

    /**
     * 创建菜单（树状结构节点），permission_type = 1
     */
    Permission createMenu(MenuCreateRequest request);

    /**
     * 查询菜单树（仅菜单类型）
     */
    List<MenuNode> getMenuTree();

    /**
     * 更新菜单
     */
    Permission updateMenu(Long id, MenuCreateRequest request);

    /**
     * 删除菜单（无子节点时才允许）
     */
    void deleteMenu(Long id);

    /**
     * 批量更新排序
     */
    void batchUpdateSort(java.util.List<MenuSortItem> items);

    /**
     * 级联删除
     */
    void deleteMenuCascade(Long id);

    /**
     * 移动菜单（调整父节点与排序），包含循环依赖校验
     */
    void moveMenu(com.enterprise.integrated.dto.MenuMoveRequest request);

    /**
     * 获取当前用户可见的菜单树（按角色权限过滤）
     */
    java.util.List<com.enterprise.integrated.dto.MenuNode> getVisibleMenuTreeForCurrentUser();
}


