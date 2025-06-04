package com.jemyeonso.app.jemyeonsobe.api.interviews.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiQuestionRequest {
    private String questionLevel;
    private String jobType;
    private String questionCategory;
    private String previousQuestion;
    private String previousAnswer;
    private Long documentId;
}

