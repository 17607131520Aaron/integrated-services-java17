package com.enterprise.integrated.controller;

import com.enterprise.integrated.annotation.OperationLog;
import com.enterprise.integrated.dto.MenuCreateRequest;
import com.enterprise.integrated.entity.Permission;
import com.enterprise.integrated.dto.MenuNode;
import com.enterprise.integrated.dto.MenuSortItem;
import com.enterprise.integrated.dto.MenuMoveRequest;
import com.enterprise.integrated.annotation.RateLimit;
import com.enterprise.integrated.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "菜单管理", description = "菜单树管理接口")
@RestController
@RequestMapping("/menus")
public class MenuController {

    private final PermissionService permissionService;

    public MenuController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Operation(summary = "新增菜单", description = "新增树状菜单节点")
    @PostMapping
    @PreAuthorize("hasAuthority('perm:system:menu:add') or hasRole('ADMIN')")
    @OperationLog(value = "新增菜单", type = OperationLog.OperationType.INSERT)
    public Permission createMenu(@Valid @RequestBody MenuCreateRequest request) {
        return permissionService.createMenu(request);
    }

    @Operation(summary = "查询菜单树", description = "获取菜单树结构")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('perm:system:menu:query') or hasRole('ADMIN')")
    @RateLimit(key = "menu_tree", count = 60, time = 60, limitType = RateLimit.LimitType.IP)
    public java.util.List<MenuNode> getMenuTree() {
        return permissionService.getMenuTree();
    }

    @Operation(summary = "更新菜单", description = "更新菜单信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('perm:system:menu:edit') or hasRole('ADMIN')")
    @OperationLog(value = "更新菜单", type = OperationLog.OperationType.UPDATE)
    public Permission updateMenu(@PathVariable Long id, @Valid @RequestBody MenuCreateRequest request) {
        return permissionService.updateMenu(id, request);
    }

    @Operation(summary = "删除菜单", description = "删除菜单（无子节点时允许）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('perm:system:menu:delete') or hasRole('ADMIN')")
    @OperationLog(value = "删除菜单", type = OperationLog.OperationType.DELETE)
    public void deleteMenu(@PathVariable Long id) {
        permissionService.deleteMenu(id);
    }

    @Operation(summary = "批量拖拽排序", description = "批量更新菜单排序")
    @PatchMapping("/sort")
    @PreAuthorize("hasAuthority('perm:system:menu:edit') or hasRole('ADMIN')")
    @OperationLog(value = "菜单排序", type = OperationLog.OperationType.UPDATE)
    public void batchSort(@Valid @RequestBody java.util.List<MenuSortItem> items) {
        permissionService.batchUpdateSort(items);
    }

    @Operation(summary = "删除菜单(可级联)", description = "cascade=true 时级联删除")
    @DeleteMapping("/{id}/cascade")
    @PreAuthorize("hasAuthority('perm:system:menu:delete') or hasRole('ADMIN')")
    @OperationLog(value = "删除菜单(级联)", type = OperationLog.OperationType.DELETE)
    public void deleteMenuCascade(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean cascade) {
        if (cascade) {
            permissionService.deleteMenuCascade(id);
        } else {
            permissionService.deleteMenu(id);
        }
    }

    @Operation(summary = "移动菜单", description = "调整菜单父子关系与排序")
    @PatchMapping("/move")
    @PreAuthorize("hasAuthority('perm:system:menu:edit') or hasRole('ADMIN')")
    @OperationLog(value = "移动菜单", type = OperationLog.OperationType.UPDATE)
    public void moveMenu(@Valid @RequestBody MenuMoveRequest request) {
        permissionService.moveMenu(request);
    }

    @Operation(summary = "可见菜单树", description = "当前登录用户可见的菜单树")
    @GetMapping("/tree/visible")
    @PreAuthorize("isAuthenticated()")
    @RateLimit(key = "visible_menu_tree", count = 120, time = 60, limitType = RateLimit.LimitType.USER)
    public java.util.List<MenuNode> getVisibleMenuTreeForCurrentUser(
            @RequestParam(defaultValue = "true") boolean menuOnly,
            @RequestParam(defaultValue = "false") boolean pruneEmpty) {
        return permissionService.getVisibleMenuTreeForCurrentUser(menuOnly, pruneEmpty);
    }
}


