package com.example.wexinplus.Message;

import com.example.wexinplus.ChatSession.ChatSession;
import com.example.wexinplus.ChatSession.ChatSessionRepository;
import com.example.wexinplus.User.User;
import com.example.wexinplus.User.UserRepository;
import com.example.wexinplus.dto.QueryMessageRequest;
import com.example.wexinplus.dto.SendMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private UserRepository userRepository;

    // 聊天记录下载目录
    private static final String DOWNLOAD_DIR = System.getProperty("user.dir") + "/downloads/chat_records/";

    /**
     * 发送消息
     */
    @Transactional
    public Message sendMessage(SendMessageRequest request) {
        ChatSession session = chatSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Message message = new Message();
        message.setChatSession(session);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType() != null ? request.getMessageType() : 1);
        message.setFilePath(request.getFilePath());
        message.setStatus(1); // 已发送

        return messageRepository.save(message);
    }

    /**
     * 获取会话的消息列表
     */
    public List<Map<String, Object>> getSessionMessages(Long sessionId) {
        List<Message> messages = messageRepository
                .findByChatSessionSessionIdOrderBySendTimeAsc(sessionId);

        return messages.stream().map(this::convertMessageToMap).collect(Collectors.toList());
    }

    /**
     * 按条件查询消息
     */
    public List<Map<String, Object>> queryMessages(QueryMessageRequest request) {
        List<Message> messages;

        if (request.getStartTime() != null && request.getEndTime() != null) {
            messages = messageRepository
                    .findByChatSessionSessionIdAndSendTimeBetweenOrderBySendTimeAsc(
                            request.getSessionId(), request.getStartTime(), request.getEndTime());
        } else if (request.getMessageType() != null) {
            messages = messageRepository
                    .findByChatSessionSessionIdAndMessageTypeOrderBySendTimeAsc(
                            request.getSessionId(), request.getMessageType());
        } else {
            messages = messageRepository
                    .findByChatSessionSessionIdOrderBySendTimeAsc(request.getSessionId());
        }

        return messages.stream().map(this::convertMessageToMap).collect(Collectors.toList());
    }

    /**
     * 下载聊天记录到本地文件
     *
     * @param sessionId 会话ID
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 下载文件的路径
     */
    public Path downloadChatRecord(Long sessionId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Message> messages;

        if (startTime != null && endTime != null) {
            messages = messageRepository
                    .findByChatSessionSessionIdAndSendTimeBetweenOrderBySendTimeAsc(
                            sessionId, startTime, endTime);
        } else {
            messages = messageRepository
                    .findByChatSessionSessionIdOrderBySendTimeAsc(sessionId);
        }

        if (messages.isEmpty()) {
            throw new RuntimeException("没有可导出的聊天记录");
        }

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        try {
            // 确保目录存在
            Path dirPath = Paths.get(DOWNLOAD_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 生成文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String sessionType = session.getSessionType() == 1 ? "private" : "group";
            String fileName = "chat_record_" + sessionType + "_" + sessionId + "_" + timestamp + ".txt";
            Path filePath = dirPath.resolve(fileName);

            // 写入文件
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                writer.write("===== 聊天记录导出 =====");
                writer.newLine();
                writer.write("会话ID: " + sessionId);
                writer.newLine();
                writer.write("会话类型: " + (session.getSessionType() == 1 ? "私聊" : "群聊"));
                writer.newLine();
                writer.write("导出时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.newLine();
                if (startTime != null && endTime != null) {
                    writer.write("时间范围: " + startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            + " ~ " + endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    writer.newLine();
                }
                writer.write("=========================");
                writer.newLine();
                writer.newLine();

                for (Message msg : messages) {
                    String senderName = msg.getSender().getNickname() != null ?
                            msg.getSender().getNickname() : msg.getSender().getUsername();
                    String timeStr = msg.getSendTime() != null ?
                            msg.getSendTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
                    String typeStr;
                    switch (msg.getMessageType()) {
                        case 1: typeStr = "[文本]"; break;
                        case 2: typeStr = "[图片]"; break;
                        case 3: typeStr = "[语音]"; break;
                        case 4: typeStr = "[文件]"; break;
                        default: typeStr = "[未知]";
                    }

                    writer.write("[" + timeStr + "] " + senderName + " " + typeStr + ": " + msg.getContent());
                    writer.newLine();
                    if (msg.getFilePath() != null) {
                        writer.write("  文件路径: " + msg.getFilePath());
                        writer.newLine();
                    }
                    writer.newLine();
                }

                writer.write("===== 导出完毕 =====");
            }

            return filePath;

        } catch (IOException e) {
            throw new RuntimeException("导出聊天记录失败: " + e.getMessage());
        }
    }

    /**
     * 将Message对象转换为Map
     */
    private Map<String, Object> convertMessageToMap(Message message) {
        Map<String, Object> map = new HashMap<>();
        map.put("messageId", message.getMessageId());
        map.put("sessionId", message.getChatSession().getSessionId());
        map.put("senderId", message.getSender().getUserId());
        map.put("senderName", message.getSender().getNickname() != null ?
                message.getSender().getNickname() : message.getSender().getUsername());
        map.put("senderAvatar", message.getSender().getAvatar());
        map.put("content", message.getContent());
        map.put("messageType", message.getMessageType());
        map.put("filePath", message.getFilePath());
        map.put("sendTime", message.getSendTime());
        map.put("status", message.getStatus());
        return map;
    }
}
