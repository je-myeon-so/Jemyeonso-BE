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
public class QuestionDetailResponseDto {
    private Long questionId;
    private Long interviewId;
    private String content;
    private String questionType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    private AnswerDetailDto answer;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AnswerDetailDto {
        private Long answerId;
        private String content;
        private String answerTime;
        private List<FeedbackDto> feedbacks;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FeedbackDto {
        private Long feedbackId;
        private String errorText;
        private String errorType;
        private String feedback;
        private String suggestion;
    }
}
