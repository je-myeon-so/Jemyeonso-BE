package com.jemyeonso.app.jemyeonsobe.api.user.service.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImproveResponseDto {
    private int code;
    private String message;
    private Data data;

    @Getter
    @Builder
    public static class Data {
        private Long interviewId;
        private String overallComment;
    }

}
