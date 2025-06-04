package com.jemyeonso.app.jemyeonsobe.api.interviews.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class InterviewResponseDto {
    private Long interviewId;
    private String question;
}
