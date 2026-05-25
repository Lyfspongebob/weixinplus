package com.example.wexinplus.SessionParticipant;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SessionParticipantRepository extends CrudRepository<SessionParticipant, Long> {

    // 查找用户参与的所有会话
    List<SessionParticipant> findByUserUserId(Long userId);

    // 查找会话的所有参与者
    List<SessionParticipant> findByChatSessionSessionId(Long sessionId);

    // 查找用户在某个会话中的参与记录
    Optional<SessionParticipant> findByUserUserIdAndChatSessionSessionId(Long userId, Long sessionId);
}
