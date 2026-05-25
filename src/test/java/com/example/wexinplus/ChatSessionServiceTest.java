package com.example.wexinplus;

import com.example.wexinplus.ChatSession.ChatSession;
import com.example.wexinplus.ChatSession.ChatSessionService;
import com.example.wexinplus.User.User;
import com.example.wexinplus.User.UserService;
import com.example.wexinplus.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ChatSessionServiceTest {

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        RegisterRequest req1 = new RegisterRequest();
        req1.setUsername("sessionuser1");
        req1.setPassword("123456");
        user1 = userService.register(req1);

        RegisterRequest req2 = new RegisterRequest();
        req2.setUsername("sessionuser2");
        req2.setPassword("123456");
        user2 = userService.register(req2);
    }

    @Test
    void testCreatePrivateSession() {
        ChatSession session = chatSessionService.createPrivateSession(user1.getUserId(), user2.getUserId());
        assertNotNull(session.getSessionId());
        assertEquals(1, session.getSessionType().intValue()); // 私聊
    }

    @Test
    void testCreatePrivateSession_ReuseExisting() {
        ChatSession session1 = chatSessionService.createPrivateSession(user1.getUserId(), user2.getUserId());
        ChatSession session2 = chatSessionService.createPrivateSession(user1.getUserId(), user2.getUserId());

        assertEquals(session1.getSessionId(), session2.getSessionId());
    }

    @Test
    void testGetUserSessions() {
        chatSessionService.createPrivateSession(user1.getUserId(), user2.getUserId());

        List<Map<String, Object>> sessions = chatSessionService.getUserSessions(user1.getUserId());
        assertFalse(sessions.isEmpty());
        assertEquals(1, sessions.size());
    }

    @Test
    void testGetSessionParticipants() {
        ChatSession session = chatSessionService.createPrivateSession(user1.getUserId(), user2.getUserId());

        List<Map<String, Object>> participants = chatSessionService.getSessionParticipants(session.getSessionId());
        assertEquals(2, participants.size());
    }

    @Test
    void testUpdateLastReadTime() {
        ChatSession session = chatSessionService.createPrivateSession(user1.getUserId(), user2.getUserId());

        chatSessionService.updateLastReadTime(user1.getUserId(), session.getSessionId());

        List<Map<String, Object>> sessions = chatSessionService.getUserSessions(user1.getUserId());
        assertNotNull(sessions.get(0).get("lastReadTime"));
    }
}
