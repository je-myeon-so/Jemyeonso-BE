package com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiQuestionResponseDto {
    private int code;
    private String message;
    private AiQuestionData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiQuestionData {
        private String question;
        private String questionType;
    }
}
