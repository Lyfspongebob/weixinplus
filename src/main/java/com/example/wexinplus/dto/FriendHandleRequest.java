package com.example.wexinplus.dto;

import lombok.Data;

@Data
public class FriendHandleRequest {
    private Long relationshipId; // 好友关系ID
    private Integer action;      // 1=通过 2=拒绝
    private String remark;       // 备注（可选）
}
