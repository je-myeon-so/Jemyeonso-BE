package com.jemyeonso.app.jemyeonsobe.api.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserFeedbackResponseDto {
    private Long userId;
    private String feedback;
}
