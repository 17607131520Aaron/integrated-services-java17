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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"menuTree", "visibleMenuTree"}, allEntries = true)
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
        // 校验父节点
        if (request.getParentId() != 0) {
            Permission parent = permissionMapper.selectById(request.getParentId());
            if (parent == null || parent.getPermissionType() == null || parent.getPermissionType() != 1 || parent.getStatus() == 0) {
                throw new BusinessException(ResultCode.PARAM_INVALID, "父菜单不存在或不可用");
            }
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
    @Cacheable(cacheNames = "menuTree", key = "'all'", unless = "#result == null || #result.isEmpty()")
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
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"menuTree", "visibleMenuTree"}, allEntries = true)
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
        // 父节点校验
        Long parentId = request.getParentId() == null ? 0L : request.getParentId();
        if (parentId != 0) {
            Permission parent = permissionMapper.selectById(parentId);
            if (parent == null || parent.getPermissionType() == null || parent.getPermissionType() != 1 || parent.getStatus() == 0) {
                throw new BusinessException(ResultCode.PARAM_INVALID, "父菜单不存在或不可用");
            }
        }

        UpdateWrapper<Permission> uw = new UpdateWrapper<>();
        uw.eq("id", id)
          .set(StringUtils.hasText(request.getName()), "permission_name", request.getName())
          .set(StringUtils.hasText(request.getCode()), "permission_code", request.getCode())
          .set("permission_type", 1)
          .set("path", request.getPath())
          .set("parent_id", parentId)
          .set(request.getSortOrder() != null, "sort_order", request.getSortOrder());
        permissionMapper.update(null, uw);
        return permissionMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"menuTree", "visibleMenuTree"}, allEntries = true)
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
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"menuTree", "visibleMenuTree"}, allEntries = true)
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
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"menuTree", "visibleMenuTree"}, allEntries = true)
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
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"menuTree", "visibleMenuTree"}, allEntries = true)
    public void moveMenu(com.enterprise.integrated.dto.MenuMoveRequest request) {
        Permission current = permissionMapper.selectById(request.getId());
        if (current == null || current.getPermissionType() == null || current.getPermissionType() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "菜单不存在");
        }
        Long newParentId = request.getNewParentId() == null ? 0L : request.getNewParentId();
        if (Objects.equals(current.getId(), newParentId)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "父节点不能是自身");
        }
        if (newParentId != 0) {
            Permission parent = permissionMapper.selectById(newParentId);
            if (parent == null || parent.getPermissionType() == null || parent.getPermissionType() != 1 || parent.getStatus() == 0) {
                throw new BusinessException(ResultCode.PARAM_INVALID, "父菜单不存在或不可用");
            }
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
        return getVisibleMenuTreeForCurrentUser(true, false);
    }

    @Override
    @Cacheable(
        cacheNames = "visibleMenuTree",
        key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication()?.getPrincipal()?.getUserId() + ':' + #menuOnly + ':' + #pruneEmpty",
        unless = "#result == null || #result.isEmpty()")
    public List<MenuNode> getVisibleMenuTreeForCurrentUser(boolean menuOnly, boolean pruneEmpty) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsServiceImpl.CustomUserDetails)) {
            return Collections.emptyList();
        }
        UserDetailsServiceImpl.CustomUserDetails user = (UserDetailsServiceImpl.CustomUserDetails) auth.getPrincipal();
        // 用户拥有的菜单（当前实现仅菜单类型）
        List<Permission> userMenus = permissionMapper.findMenusByUserId(user.getUserId());
        if (userMenus.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询所有菜单，用于补齐祖先节点
        List<Permission> allMenus = permissionMapper.selectList(new QueryWrapper<Permission>()
                .eq("permission_type", 1)
                .eq("deleted", 0));
        Map<Long, Permission> allMap = new HashMap<>();
        for (Permission p : allMenus) {
            allMap.put(p.getId(), p);
        }

        // 计算应包含的ID集合 = 用户直接拥有 + 其所有祖先
        Set<Long> includeIds = new LinkedHashSet<>();
        for (Permission p : userMenus) {
            Long cur = p.getId();
            while (cur != null && cur != 0 && includeIds.add(cur)) {
                Permission parent = allMap.get(cur);
                if (parent == null) break;
                cur = parent.getParentId();
                if (cur != null && cur == 0) {
                    includeIds.add(parent.getId());
                }
            }
            // 补齐当前节点的直接父级（处理父级为0或null场景）
            if (p.getParentId() != null) {
                includeIds.add(p.getParentId());
            }
        }

        // 按 parentId/sort/id 排序后构建树
        List<Permission> menus = new ArrayList<>();
        for (Long id : includeIds) {
            Permission pm = allMap.get(id);
            if (pm != null) menus.add(pm);
        }
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
        if (pruneEmpty) {
            roots = pruneEmptyNodes(roots);
        }
        return roots;
    }

    private List<MenuNode> pruneEmptyNodes(List<MenuNode> nodes) {
        List<MenuNode> result = new ArrayList<>();
        for (MenuNode node : nodes) {
            List<MenuNode> prunedChildren = pruneEmptyNodes(node.getChildren());
            node.setChildren(prunedChildren);
            if (prunedChildren.isEmpty()) {
                // 叶子节点，保留（保守策略：菜单节点即保留）
                result.add(node);
            } else {
                result.add(node);
            }
        }
        return result;
    }
}


