package com.jemyeonso.app.jemyeonsobe.api.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoRequestDto {
    private String nickname;
    private String profileImgUrl;
    private String comment;
}
