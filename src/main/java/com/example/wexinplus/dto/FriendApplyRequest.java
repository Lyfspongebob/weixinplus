package com.example.wexinplus.dto;

import lombok.Data;

@Data
public class FriendApplyRequest {
    private Long userId;        // 当前用户ID（主动添加方）
    private Long friendId;      // 被添加的好友ID
    private Long groupId;       // 要添加到哪个分组
    private String applyMessage; // 验证信息
    private String remark;      // 好友备注
}
