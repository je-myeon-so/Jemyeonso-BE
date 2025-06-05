package com.jemyeonso.app.jemyeonsobe.api.interviews.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class InterviewDetailsResponse {
    private Long interviewId;
    private Long userId;
    private FileDto file;
    private String type;
    private String difficulty;
    private String job;
    private LocalDateTime createdAt;
    private List<QuestionDetailsDto> questions;
}
