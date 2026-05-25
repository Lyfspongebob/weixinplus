package com.example.wexinplus.ChatSession;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChatSessionRepository extends CrudRepository<ChatSession, Long> {

    // 根据会话类型查找
    List<ChatSession> findBySessionType(Integer sessionType);
}
