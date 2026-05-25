package com.example.wexinplus.User;

import com.example.wexinplus.FriendGroup.FriendGroup;
import com.example.wexinplus.FriendGroup.FriendGroupRepository;
import com.example.wexinplus.dto.LoginRequest;
import com.example.wexinplus.dto.RegisterRequest;
import com.example.wexinplus.dto.UpdateUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendGroupRepository friendGroupRepository;

    /**
     * 用户注册
     */
    @Transactional
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(0); // 默认离线
        user = userRepository.save(user);

        // 为新用户创建默认好友分组："我的好友"
        FriendGroup defaultGroup = new FriendGroup();
        defaultGroup.setUser(user);
        defaultGroup.setGroupName("我的好友");
        friendGroupRepository.save(defaultGroup);

        return user;
    }

    /**
     * 用户登录
     */
    public User login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户名或密码错误");
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 更新用户状态为在线
        user.setStatus(1);
        userRepository.save(user);

        return user;
    }

    /**
     * 用户登出
     */
    @Transactional
    public void logout(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(0); // 设置为离线
            userRepository.save(user);
        }
    }

    /**
     * 根据ID获取用户信息
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 根据用户名搜索用户
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 模糊搜索用户（支持部分匹配）
     */
    public List<User> searchUsersByUsername(String username) {
        return userRepository.findByUsernameContaining(username);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(UpdateUserRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        return userRepository.save(user);
    }
}
