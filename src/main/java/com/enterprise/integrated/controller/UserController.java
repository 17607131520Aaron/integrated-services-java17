package com.enterprise.integrated.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.integrated.dto.UserDTO;
import com.enterprise.integrated.entity.User;
import com.enterprise.integrated.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "分页查询用户", description = "分页查询用户列表")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public IPage<User> pageUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "邮箱") @RequestParam(required = false) String email,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        Page<User> page = new Page<>(current, size);
        return userService.pageUsers(page, username, email, status);
    }

    @Operation(summary = "根据ID查询用户", description = "根据用户ID查询用户详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.userId == #id")
    public User getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        return userService.getById(id);
    }

    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(@Valid @RequestBody UserDTO userDTO) {
        return userService.createUser(userDTO);
    }

    @Operation(summary = "更新用户", description = "更新用户信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.userId == #id")
    public User updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO);
    }

    @Operation(summary = "删除用户", description = "删除用户(逻辑删除)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.removeById(id);
    }

    @Operation(summary = "启用/禁用用户", description = "更新用户状态")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态(0:禁用,1:启用)") @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
    }

    @Operation(summary = "重置密码", description = "重置用户密码")
    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public void resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
    }

    @Operation(summary = "检查用户名", description = "检查用户名是否已存在")
    @GetMapping("/check/username")
    public Boolean checkUsername(@Parameter(description = "用户名") @RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return !exists;
    }

    @Operation(summary = "检查邮箱", description = "检查邮箱是否已存在")
    @GetMapping("/check/email")
    public Boolean checkEmail(@Parameter(description = "邮箱") @RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return !exists;
    }

    @Operation(summary = "检查手机号", description = "检查手机号是否已存在")
    @GetMapping("/check/phone")
    public Boolean checkPhone(@Parameter(description = "手机号") @RequestParam String phone) {
        boolean exists = userService.existsByPhone(phone);
        return !exists;
    }
}
