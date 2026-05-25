package com.example.wexinplus.dto;

import lombok.Data;

@Data
public class FriendMoveRequest {
    private Long relationshipId; // 好友关系ID
    private Long newGroupId;     // 目标分组ID
}
