package com.jemyeonso.app.jemyeonsobe.api.interviews.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiQuestionResponse {
    private int code;
    private String message;
    private AiQuestionData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiQuestionData {
        private String question;
        private String questionType; // 예: "TECHNOLOGY", "PERSONAL" 등
    }
}
