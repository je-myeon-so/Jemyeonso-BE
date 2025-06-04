package com.jemyeonso.app.jemyeonsobe.api.interviews.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionRequestDto {
    private Long interviewId;
    private String questionLevel;
    private String jobType;
    private String questionCategory;
    private String previousQuestion;
    private String previousAnswer;
    private Long documentId;
}
