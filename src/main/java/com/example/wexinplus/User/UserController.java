package com.example.wexinplus.User;

import com.example.wexinplus.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            // 返回时隐藏密码
            user.setPassword(null);
            return ResponseEntity.ok(ApiResponse.success("注册成功", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 用户登录
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse loginResponse = userService.login(request);
            loginResponse.getUser().setPassword(null);
            return ResponseEntity.ok(ApiResponse.success("登录成功", loginResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 用户登出
     * POST /api/users/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        userService.logout();
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }

    /**
     * 获取当前登录用户信息
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        try {
            long userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
            User user = userService.getUserById(userId);
            user.setPassword(null);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取用户信息
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            user.setPassword(null);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据用户名搜索用户
     * GET /api/users/search?username=xxx
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<User>>> searchByUsername(@RequestParam String username) {
        List<User> users = userService.searchUsersByUsername(username);
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * 更新用户信息
     * PUT /api/users/update
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<User>> updateUser(@RequestBody UpdateUserRequest request) {
        try {
            User user = userService.updateUser(request);
            user.setPassword(null);
            return ResponseEntity.ok(ApiResponse.success("更新成功", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
