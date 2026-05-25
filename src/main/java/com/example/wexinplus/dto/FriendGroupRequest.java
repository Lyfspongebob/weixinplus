package com.example.wexinplus.dto;

import lombok.Data;

@Data
public class FriendGroupRequest {
    private Long userId;
    private String groupName;
}
