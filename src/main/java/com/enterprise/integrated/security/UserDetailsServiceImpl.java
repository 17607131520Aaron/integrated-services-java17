package com.enterprise.integrated.security;

import com.enterprise.integrated.entity.User;
import com.enterprise.integrated.entity.Role;
import com.enterprise.integrated.service.UserService;
import com.enterprise.integrated.mapper.RoleMapper;
import com.enterprise.integrated.mapper.PermissionMapper;
import com.enterprise.integrated.entity.Permission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 用户详情服务实现
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public UserDetailsServiceImpl(UserService userService, RoleMapper roleMapper, PermissionMapper permissionMapper) {
        this.userService = userService;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        return new CustomUserDetails(user, roleMapper, permissionMapper);
    }

    /**
     * 自定义用户详情类
     */
    public static class CustomUserDetails implements UserDetails {
        private final User user;
        private final RoleMapper roleMapper;
        private final PermissionMapper permissionMapper;

        public CustomUserDetails(User user, RoleMapper roleMapper, PermissionMapper permissionMapper) {
            this.user = user;
            this.roleMapper = roleMapper;
            this.permissionMapper = permissionMapper;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = new ArrayList<>();
            List<Role> roles = roleMapper.findRolesByUserId(user.getId());
            for (Role role : roles) {
                if (role.getRoleCode() != null) {
                    authorities.add(new SimpleGrantedAuthority(role.getRoleCode()));
                }
            }
            // 加载权限点
            List<Permission> permissions = permissionMapper.findPermissionsByUserId(user.getId());
            for (Permission permission : permissions) {
                if (permission.getPermissionCode() != null) {
                    authorities.add(new SimpleGrantedAuthority("perm:" + permission.getPermissionCode()));
                }
            }
            if (authorities.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getStatus() == 1;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getStatus() == 1;
        }

        public User getUser() {
            return user;
        }

        public Long getUserId() {
            return user.getId();
        }
    }
}
