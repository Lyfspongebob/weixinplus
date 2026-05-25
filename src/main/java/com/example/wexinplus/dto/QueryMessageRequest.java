package com.example.wexinplus.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QueryMessageRequest {
    private Long sessionId;         // 会话ID
    private LocalDateTime startTime; // 开始时间（可选）
    private LocalDateTime endTime;   // 结束时间（可选）
    private Integer messageType;     // 消息类型（可选）
}
