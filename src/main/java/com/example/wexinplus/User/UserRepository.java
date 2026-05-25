package com.example.wexinplus.User;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    // 通过用户名查找用户
    Optional< User> findByUsername(String username);

    // 新增：检查用户名是否存在（关键方法）
    boolean existsByUsername(String username);
}
