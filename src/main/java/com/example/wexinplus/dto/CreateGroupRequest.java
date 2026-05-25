package com.example.wexinplus.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequest {
    private Long ownerId;       // 群主ID
    private String groupName;   // 群名称
    private String notice;      // 群公告（可选）
    private List<Long> memberIds; // 初始成员ID列表（可选）
}
