package com.jemyeonso.app.jemyeonsobe.api.interviews.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class InterviewQuestionsResponseDto {
    private Long interviewId;
    private Long documentId;
    private Long userId;
    private String title;
    private String questionCategory;
    private String questionLevel;
    private String jobtype;
    private Integer totalScore;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    private List<QuestionSummaryDto> questions;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class QuestionSummaryDto {
        private Long questionId;
        private String content;
        private String questionType;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime createdAt;
    }
}
