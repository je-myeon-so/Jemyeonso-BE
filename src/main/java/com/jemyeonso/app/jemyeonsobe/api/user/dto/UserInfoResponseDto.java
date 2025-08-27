package com.jemyeonso.app.jemyeonsobe.api.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponseDto {
    private Long userId;
    private String profileImgUrl;
    private String name;
    private String nickname;
    private String email;
    private String comment;
}
