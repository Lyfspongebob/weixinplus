package com.example.wexinplus.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateSessionRequest {
    private Integer sessionType; // 1=私聊 2=群聊
    private List<Long> userIds;  // 参与者ID列表（私聊时传2个用户ID）
    private Long groupId;        // 群聊ID（如果是群聊会话）
}
