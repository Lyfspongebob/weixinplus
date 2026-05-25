package com.example.wexinplus.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long sessionId;     // 会话ID
    private Long senderId;      // 发送者ID
    private String content;     // 消息内容
    private Integer messageType; // 1.text 2.image 3.voice 4.file
    private String filePath;    // 文件路径（图片/语音/文件时使用）
}
