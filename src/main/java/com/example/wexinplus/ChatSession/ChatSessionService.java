package com.example.wexinplus.ChatSession;

import com.example.wexinplus.SessionParticipant.SessionParticipant;
import com.example.wexinplus.SessionParticipant.SessionParticipantRepository;
import com.example.wexinplus.User.User;
import com.example.wexinplus.User.UserRepository;
import com.example.wexinplus.dto.CreateSessionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatSessionService {

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private SessionParticipantRepository sessionParticipantRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 创建私聊会话（如果已存在则返回现有会话）
     */
    @Transactional
    public ChatSession createPrivateSession(Long userId1, Long userId2) {
        // 查找两个用户之间是否已存在私聊会话
        List<SessionParticipant> user1Sessions = sessionParticipantRepository.findByUserUserId(userId1);

        for (SessionParticipant sp1 : user1Sessions) {
            if (sp1.getChatSession().getSessionType() == 1) { // 私聊会话
                List<SessionParticipant> participants = sessionParticipantRepository
                        .findByChatSessionSessionId(sp1.getChatSession().getSessionId());

                // 检查是否包含另一个用户
                boolean hasUser2 = participants.stream()
                        .anyMatch(p -> p.getUser().getUserId().equals(userId2));

                if (hasUser2 && participants.size() == 2) {
                    return sp1.getChatSession(); // 返回现有会话
                }
            }
        }

        // 创建新会话
        ChatSession session = new ChatSession();
        session.setSessionType(1); // 私聊
        session = chatSessionRepository.save(session);

        // 添加参与者
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        addParticipant(session, user1);
        addParticipant(session, user2);

        return session;
    }

    /**
     * 获取用户的所有会话列表
     */
    public List<Map<String, Object>> getUserSessions(Long userId) {
        List<SessionParticipant> participants = sessionParticipantRepository.findByUserUserId(userId);

        return participants.stream().map(sp -> {
            ChatSession session = sp.getChatSession();
            Map<String, Object> sessionMap = new HashMap<>();
            sessionMap.put("sessionId", session.getSessionId());
            sessionMap.put("sessionType", session.getSessionType());
            sessionMap.put("createTime", session.getCreateTime());
            sessionMap.put("lastReadTime", sp.getLastReadTime());

            if (session.getSessionType() == 1) {
                // 私聊：获取对方用户信息
                List<SessionParticipant> allParticipants = sessionParticipantRepository
                        .findByChatSessionSessionId(session.getSessionId());
                User otherUser = allParticipants.stream()
                        .filter(p -> !p.getUser().getUserId().equals(userId))
                        .findFirst()
                        .map(SessionParticipant::getUser)
                        .orElse(null);

                if (otherUser != null) {
                    sessionMap.put("targetUserId", otherUser.getUserId());
                    sessionMap.put("targetUsername", otherUser.getUsername());
                    sessionMap.put("targetNickname", otherUser.getNickname());
                    sessionMap.put("targetAvatar", otherUser.getAvatar());
                }
            } else if (session.getSessionType() == 2 && session.getChatGroup() != null) {
                // 群聊：获取群信息
                sessionMap.put("groupId", session.getChatGroup().getGroupId());
                sessionMap.put("groupName", session.getChatGroup().getGroupName());
            }

            return sessionMap;
        }).collect(Collectors.toList());
    }

    /**
     * 获取会话的参与者列表
     */
    public List<Map<String, Object>> getSessionParticipants(Long sessionId) {
        List<SessionParticipant> participants = sessionParticipantRepository
                .findByChatSessionSessionId(sessionId);

        return participants.stream().map(p -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", p.getUser().getUserId());
            userMap.put("username", p.getUser().getUsername());
            userMap.put("nickname", p.getUser().getNickname());
            userMap.put("avatar", p.getUser().getAvatar());
            userMap.put("status", p.getUser().getStatus());
            return userMap;
        }).collect(Collectors.toList());
    }

    /**
     * 更新用户最后阅读时间
     */
    @Transactional
    public void updateLastReadTime(Long userId, Long sessionId) {
        sessionParticipantRepository
                .findByUserUserIdAndChatSessionSessionId(userId, sessionId)
                .ifPresent(sp -> {
                    sp.setLastReadTime(java.time.LocalDateTime.now());
                    sessionParticipantRepository.save(sp);
                });
    }

    // ==================== 私有方法 ====================

    private void addParticipant(ChatSession session, User user) {
        SessionParticipant participant = new SessionParticipant();
        participant.setChatSession(session);
        participant.setUser(user);
        sessionParticipantRepository.save(participant);
    }
}
