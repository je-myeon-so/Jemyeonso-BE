package com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAnswerAnalyzeRequestDto {
    private String questionLevel;
    private String jobType;
    private String questionCategory;
    private String question;
    private String answer;
}
