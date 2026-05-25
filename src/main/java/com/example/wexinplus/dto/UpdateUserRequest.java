package com.example.wexinplus.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private Long userId;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
}
