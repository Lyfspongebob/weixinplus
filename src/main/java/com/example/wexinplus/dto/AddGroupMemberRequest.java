package com.example.wexinplus.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddGroupMemberRequest {
    private Long groupId;       // 群ID
    private Long operatorId;    // 操作人ID（群主或管理员）
    private List<Long> memberIds; // 要添加的成员ID列表
}
