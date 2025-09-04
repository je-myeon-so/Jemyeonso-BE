package com.jemyeonso.app.jemyeonsobe.api.user.service.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImproveRequestDto {
    private Long interviewId;
    private Long documentId;
    private String jobType;
}
