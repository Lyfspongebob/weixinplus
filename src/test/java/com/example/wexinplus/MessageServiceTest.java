package com.example.wexinplus;

import com.example.wexinplus.ChatGroup.ChatGroup;
import com.example.wexinplus.ChatGroup.ChatGroupService;
import com.example.wexinplus.ChatSession.ChatSession;
import com.example.wexinplus.ChatSession.ChatSessionService;
import com.example.wexinplus.Message.Message;
import com.example.wexinplus.Message.MessageService;
import com.example.wexinplus.User.User;
import com.example.wexinplus.User.UserService;
import com.example.wexinplus.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatGroupService chatGroupService;

    private User user1;
    private User user2;
    private ChatSession privateSession;

    @BeforeEach
    void setUp() {
        RegisterRequest req1 = new RegisterRequest();
        req1.setUsername("msguser1");
        req1.setPassword("123456");
        req1.setNickname("消息用户1");
        user1 = userService.register(req1);

        RegisterRequest req2 = new RegisterRequest();
        req2.setUsername("msguser2");
        req2.setPassword("123456");
        req2.setNickname("消息用户2");
        user2 = userService.register(req2);

        privateSession = chatSessionService.createPrivateSession(user1.getUserId(), user2.getUserId());
    }

    @Test
    void testSendMessage_Text() {
        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(privateSession.getSessionId());
        request.setSenderId(user1.getUserId());
        request.setContent("你好，这是一条测试消息");
        request.setMessageType(1);

        Message message = messageService.sendMessage(request);
        assertNotNull(message.getMessageId());
        assertEquals("你好，这是一条测试消息", message.getContent());
        assertEquals(1, message.getMessageType().intValue());
        assertEquals(1, message.getStatus().intValue()); // 已发送
    }

    @Test
    void testSendMessage_Voice() {
        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(privateSession.getSessionId());
        request.setSenderId(user1.getUserId());
        request.setContent("[语音消息]");
        request.setMessageType(3); // 语音
        request.setFilePath("/uploads/voices/test.amr");

        Message message = messageService.sendMessage(request);
        assertNotNull(message.getMessageId());
        assertEquals(3, message.getMessageType().intValue());
        assertEquals("/uploads/voices/test.amr", message.getFilePath());
    }

    @Test
    void testGetSessionMessages() {
        // 发送多条消息
        SendMessageRequest req1 = new SendMessageRequest();
        req1.setSessionId(privateSession.getSessionId());
        req1.setSenderId(user1.getUserId());
        req1.setContent("消息1");
        req1.setMessageType(1);
        messageService.sendMessage(req1);

        SendMessageRequest req2 = new SendMessageRequest();
        req2.setSessionId(privateSession.getSessionId());
        req2.setSenderId(user2.getUserId());
        req2.setContent("消息2");
        req2.setMessageType(1);
        messageService.sendMessage(req2);

        List<Map<String, Object>> messages = messageService.getSessionMessages(privateSession.getSessionId());
        assertEquals(2, messages.size());
    }

    @Test
    void testQueryMessages_ByTimeRange() {
        SendMessageRequest req1 = new SendMessageRequest();
        req1.setSessionId(privateSession.getSessionId());
        req1.setSenderId(user1.getUserId());
        req1.setContent("旧消息");
        req1.setMessageType(1);
        messageService.sendMessage(req1);

        try { Thread.sleep(100); } catch (InterruptedException e) {}

        LocalDateTime midTime = LocalDateTime.now();

        try { Thread.sleep(100); } catch (InterruptedException e) {}

        SendMessageRequest req2 = new SendMessageRequest();
        req2.setSessionId(privateSession.getSessionId());
        req2.setSenderId(user1.getUserId());
        req2.setContent("新消息");
        req2.setMessageType(1);
        messageService.sendMessage(req2);

        QueryMessageRequest queryRequest = new QueryMessageRequest();
        queryRequest.setSessionId(privateSession.getSessionId());
        queryRequest.setStartTime(midTime);
        queryRequest.setEndTime(LocalDateTime.now());

        List<Map<String, Object>> results = messageService.queryMessages(queryRequest);
        assertEquals(1, results.size());
        assertEquals("新消息", results.get(0).get("content"));
    }

    @Test
    void testQueryMessages_ByType() {
        SendMessageRequest req1 = new SendMessageRequest();
        req1.setSessionId(privateSession.getSessionId());
        req1.setSenderId(user1.getUserId());
        req1.setContent("文本消息");
        req1.setMessageType(1);
        messageService.sendMessage(req1);

        SendMessageRequest req2 = new SendMessageRequest();
        req2.setSessionId(privateSession.getSessionId());
        req2.setSenderId(user1.getUserId());
        req2.setContent("[图片]");
        req2.setMessageType(2);
        req2.setFilePath("/uploads/images/test.jpg");
        messageService.sendMessage(req2);

        QueryMessageRequest queryRequest = new QueryMessageRequest();
        queryRequest.setSessionId(privateSession.getSessionId());
        queryRequest.setMessageType(2);

        List<Map<String, Object>> results = messageService.queryMessages(queryRequest);
        assertEquals(1, results.size());
        assertEquals(2, results.get(0).get("messageType"));
    }

    @Test
    void testDownloadChatRecord() {
        SendMessageRequest req1 = new SendMessageRequest();
        req1.setSessionId(privateSession.getSessionId());
        req1.setSenderId(user1.getUserId());
        req1.setContent("第一条消息");
        req1.setMessageType(1);
        messageService.sendMessage(req1);

        SendMessageRequest req2 = new SendMessageRequest();
        req2.setSessionId(privateSession.getSessionId());
        req2.setSenderId(user2.getUserId());
        req2.setContent("第二条消息");
        req2.setMessageType(1);
        messageService.sendMessage(req2);

        String filePath = messageService.downloadChatRecord(privateSession.getSessionId(), null, null);
        assertNotNull(filePath);
        assertTrue(filePath.contains("chat_record"));
    }

    @Test
    void testDownloadChatRecord_WithTimeRange() {
        SendMessageRequest req1 = new SendMessageRequest();
        req1.setSessionId(privateSession.getSessionId());
        req1.setSenderId(user1.getUserId());
        req1.setContent("旧消息");
        req1.setMessageType(1);
        messageService.sendMessage(req1);

        try { Thread.sleep(100); } catch (InterruptedException e) {}

        LocalDateTime start = LocalDateTime.now();

        try { Thread.sleep(100); } catch (InterruptedException e) {}

        SendMessageRequest req2 = new SendMessageRequest();
        req2.setSessionId(privateSession.getSessionId());
        req2.setSenderId(user1.getUserId());
        req2.setContent("新消息");
        req2.setMessageType(1);
        messageService.sendMessage(req2);

        String filePath = messageService.downloadChatRecord(privateSession.getSessionId(), start, LocalDateTime.now());
        assertNotNull(filePath);
    }

    @Test
    void testSendMessage_ToGroupSession() {
        // 创建群聊
        CreateGroupRequest groupRequest = new CreateGroupRequest();
        groupRequest.setOwnerId(user1.getUserId());
        groupRequest.setGroupName("测试群");
        groupRequest.setMemberIds(List.of(user2.getUserId()));
        ChatGroup group = chatGroupService.createGroup(groupRequest);

        // 发送群消息
        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(group.getChatSession().getSessionId());
        request.setSenderId(user1.getUserId());
        request.setContent("群消息测试");
        request.setMessageType(1);

        Message message = messageService.sendMessage(request);
        assertNotNull(message.getMessageId());
        assertEquals("群消息测试", message.getContent());
    }
}
