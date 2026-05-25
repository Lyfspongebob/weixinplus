package com.example.wexinplus;

import com.example.wexinplus.User.User;
import com.example.wexinplus.User.UserRepository;
import com.example.wexinplus.User.UserService;
import com.example.wexinplus.dto.LoginRequest;
import com.example.wexinplus.dto.RegisterRequest;
import com.example.wexinplus.dto.UpdateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("123456");
        registerRequest.setNickname("测试用户");
        registerRequest.setPhone("13800138000");
        registerRequest.setEmail("test@example.com");
    }

    @Test
    void testRegister_Success() {
        User user = userService.register(registerRequest);
        assertNotNull(user.getUserId());
        assertEquals("testuser", user.getUsername());
        assertEquals("测试用户", user.getNickname());
        assertEquals(0, user.getStatus()); // 默认离线
    }

    @Test
    void testRegister_DuplicateUsername() {
        userService.register(registerRequest);
        assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });
    }

    @Test
    void testLogin_Success() {
        userService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("123456");

        User user = userService.login(loginRequest);
        assertNotNull(user);
        assertEquals(1, user.getStatus()); // 登录后在线
    }

    @Test
    void testLogin_WrongPassword() {
        userService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
    }

    @Test
    void testLogin_UserNotFound() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("123456");

        assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
    }

    @Test
    void testLogout() {
        User user = userService.register(registerRequest);
        userService.logout(user.getUserId());

        User loggedOutUser = userRepository.findById(user.getUserId()).orElseThrow();
        assertEquals(0, loggedOutUser.getStatus());
    }

    @Test
    void testGetUserById() {
        User user = userService.register(registerRequest);
        User found = userService.getUserById(user.getUserId());
        assertNotNull(found);
        assertEquals(user.getUserId(), found.getUserId());
    }

    @Test
    void testGetUserById_NotFound() {
        assertThrows(RuntimeException.class, () -> {
            userService.getUserById(99999L);
        });
    }

    @Test
    void testUpdateUser() {
        User user = userService.register(registerRequest);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUserId(user.getUserId());
        updateRequest.setNickname("新昵称");
        updateRequest.setPhone("13900139000");

        User updated = userService.updateUser(updateRequest);
        assertEquals("新昵称", updated.getNickname());
        assertEquals("13900139000", updated.getPhone());
    }
}
