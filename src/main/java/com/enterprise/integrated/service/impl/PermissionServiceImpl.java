package com.enterprise.integrated.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.enterprise.integrated.common.exception.BusinessException;
import com.enterprise.integrated.common.result.ResultCode;
import com.enterprise.integrated.dto.MenuCreateRequest;
import com.enterprise.integrated.dto.MenuNode;
import com.enterprise.integrated.entity.Permission;
import com.enterprise.integrated.mapper.PermissionMapper;
import com.enterprise.integrated.service.PermissionService;
import com.enterprise.integrated.dto.MenuSortItem;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.enterprise.integrated.security.UserDetailsServiceImpl;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Override
    public Permission createMenu(MenuCreateRequest request) {
        if (!StringUtils.hasText(request.getName()) || !StringUtils.hasText(request.getCode())) {
            throw new BusinessException(ResultCode.PARAM_MISSING, "菜单名称或编码不能为空");
        }

        // 校验编码唯一
        Long count = permissionMapper.selectCount(new QueryWrapper<Permission>()
                .eq("permission_code", request.getCode())
                .eq("deleted", 0));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "菜单编码已存在");
        }

        // 根节点 parentId 允许 0；若非 0 可进一步校验父节点存在
        if (request.getParentId() == null) {
            request.setParentId(0L);
        }

        Permission permission = new Permission();
        permission.setPermissionName(request.getName());
        permission.setPermissionCode(request.getCode());
        permission.setPermissionType(1); // 1: 菜单
        permission.setPath(request.getPath());
        permission.setMethod(null);
        permission.setStatus(1);
        permission.setParentId(request.getParentId());
        permission.setSortOrder(request.getSortOrder());

        // 使用 BaseEntity 字段：parent_id 不在 Permission 中，存放到表字段
        // 这里通过自定义插入SQL更优；简化处理使用原生Mapper XML更灵活，这里先直接insert并依赖默认列
        permissionMapper.insert(permission);

        return permissionMapper.selectById(permission.getId());
    }

    @Override
    public List<MenuNode> getMenuTree() {
        List<Permission> menus = permissionMapper.selectList(new QueryWrapper<Permission>()
                .eq("permission_type", 1)
                .eq("deleted", 0)
                .orderByAsc("parent_id", "sort_order", "id"));

        Map<Long, MenuNode> idToNode = new LinkedHashMap<>();
        List<MenuNode> roots = new ArrayList<>();

        for (Permission p : menus) {
            MenuNode node = new MenuNode();
            node.setId(p.getId());
            node.setParentId(p.getParentId());
            node.setName(p.getPermissionName());
            node.setCode(p.getPermissionCode());
            node.setPath(p.getPath());
            node.setSortOrder(p.getSortOrder());
            idToNode.put(p.getId(), node);
        }

        for (MenuNode node : idToNode.values()) {
            Long parentId = node.getParentId() == null ? 0L : node.getParentId();
            if (parentId == 0) {
                roots.add(node);
            } else {
                MenuNode parent = idToNode.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }

    @Override
    public Permission updateMenu(Long id, MenuCreateRequest request) {
        Permission exist = permissionMapper.selectById(id);
        if (exist == null || exist.getPermissionType() == null || exist.getPermissionType() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "菜单不存在");
        }
        // 编码唯一
        if (StringUtils.hasText(request.getCode())) {
            Long cnt = permissionMapper.selectCount(new QueryWrapper<Permission>()
                    .eq("permission_code", request.getCode())
                    .ne("id", id)
                    .eq("deleted", 0));
            if (cnt != null && cnt > 0) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "菜单编码已存在");
            }
        }
        UpdateWrapper<Permission> uw = new UpdateWrapper<>();
        uw.eq("id", id)
          .set(StringUtils.hasText(request.getName()), "permission_name", request.getName())
          .set(StringUtils.hasText(request.getCode()), "permission_code", request.getCode())
          .set("permission_type", 1)
          .set("path", request.getPath())
          .set("parent_id", request.getParentId() == null ? 0L : request.getParentId())
          .set(request.getSortOrder() != null, "sort_order", request.getSortOrder());
        permissionMapper.update(null, uw);
        return permissionMapper.selectById(id);
    }

    @Override
    public void deleteMenu(Long id) {
        // 有子节点不可删
        Long children = permissionMapper.selectCount(new QueryWrapper<Permission>()
                .eq("parent_id", id)
                .eq("deleted", 0));
        if (children != null && children > 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "请先删除子菜单");
        }
        UpdateWrapper<Permission> uw = new UpdateWrapper<>();
        uw.eq("id", id).set("deleted", 1);
        permissionMapper.update(null, uw);
    }

    @Override
    public void batchUpdateSort(List<MenuSortItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (MenuSortItem item : items) {
            UpdateWrapper<Permission> uw = new UpdateWrapper<>();
            uw.eq("id", item.getId()).set("sort_order", item.getSortOrder());
            permissionMapper.update(null, uw);
        }
    }

    @Override
    public void deleteMenuCascade(Long id) {
        // 递归标记删除
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(id);
        while (!stack.isEmpty()) {
            Long current = stack.pop();
            UpdateWrapper<Permission> uw = new UpdateWrapper<>();
            uw.eq("id", current).set("deleted", 1);
            permissionMapper.update(null, uw);

            List<Permission> children = permissionMapper.selectList(new QueryWrapper<Permission>()
                    .eq("parent_id", current)
                    .eq("deleted", 0));
            for (Permission child : children) {
                stack.push(child.getId());
            }
        }
    }

    @Override
    public void moveMenu(com.enterprise.integrated.dto.MenuMoveRequest request) {
        Permission current = permissionMapper.selectById(request.getId());
        if (current == null || current.getPermissionType() == null || current.getPermissionType() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "菜单不存在");
        }
        Long newParentId = request.getNewParentId() == null ? 0L : request.getNewParentId();
        if (Objects.equals(current.getId(), newParentId)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "父节点不能是自身");
        }
        // 循环依赖校验：新父节点不能是当前节点的子孙
        if (newParentId != 0) {
            Deque<Long> stack = new ArrayDeque<>();
            stack.push(current.getId());
            while (!stack.isEmpty()) {
                Long c = stack.pop();
                if (Objects.equals(c, newParentId)) {
                    throw new BusinessException(ResultCode.PARAM_INVALID, "不能将节点移动到其子孙节点下");
                }
                List<Permission> children = permissionMapper.selectList(new QueryWrapper<Permission>()
                        .eq("parent_id", c)
                        .eq("deleted", 0));
                for (Permission child : children) {
                    stack.push(child.getId());
                }
            }
        }
        UpdateWrapper<Permission> uw = new UpdateWrapper<>();
        uw.eq("id", current.getId())
          .set("parent_id", newParentId)
          .set("sort_order", request.getNewSortOrder());
        permissionMapper.update(null, uw);
    }

    @Override
    public List<MenuNode> getVisibleMenuTreeForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsServiceImpl.CustomUserDetails)) {
            return Collections.emptyList();
        }
        UserDetailsServiceImpl.CustomUserDetails user = (UserDetailsServiceImpl.CustomUserDetails) auth.getPrincipal();
        List<Permission> menus = permissionMapper.findMenusByUserId(user.getUserId());
        menus.sort(Comparator.comparing(Permission::getParentId, Comparator.nullsFirst(Long::compareTo))
                .thenComparing(p -> Optional.ofNullable(p.getSortOrder()).orElse(0))
                .thenComparing(Permission::getId));

        Map<Long, MenuNode> idToNode = new LinkedHashMap<>();
        List<MenuNode> roots = new ArrayList<>();
        for (Permission p : menus) {
            MenuNode node = new MenuNode();
            node.setId(p.getId());
            node.setParentId(p.getParentId());
            node.setName(p.getPermissionName());
            node.setCode(p.getPermissionCode());
            node.setPath(p.getPath());
            node.setSortOrder(p.getSortOrder());
            idToNode.put(p.getId(), node);
        }
        for (MenuNode node : idToNode.values()) {
            Long parentId = node.getParentId() == null ? 0L : node.getParentId();
            if (parentId == 0) {
                roots.add(node);
            } else {
                MenuNode parent = idToNode.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }
}


