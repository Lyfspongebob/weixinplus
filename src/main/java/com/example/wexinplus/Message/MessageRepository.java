package com.example.wexinplus.Message;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Long> {

    // 根据会话ID查找所有消息（按发送时间升序）
    List<Message> findByChatSessionSessionIdOrderBySendTimeAsc(Long sessionId);

    // 根据会话ID和发送时间范围查找消息
    List<Message> findByChatSessionSessionIdAndSendTimeBetweenOrderBySendTimeAsc(
            Long sessionId, LocalDateTime startTime, LocalDateTime endTime);

    // 根据发送者ID查找消息
    List<Message> findBySenderUserIdOrderBySendTimeDesc(Long senderId);

    // 根据消息类型查找会话中的消息
    List<Message> findByChatSessionSessionIdAndMessageTypeOrderBySendTimeAsc(
            Long sessionId, Integer messageType);
}
