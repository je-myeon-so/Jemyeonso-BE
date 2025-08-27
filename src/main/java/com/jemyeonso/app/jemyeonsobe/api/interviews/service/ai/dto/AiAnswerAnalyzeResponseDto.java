package com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiAnswerAnalyzeResponseDto {
    private int code;
    private String message;
    private Data data;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private List<Analysis> analysis;
    }

    @Getter
    @NoArgsConstructor

    public static class Analysis {
        private String errorText;
        private String errorType;
        private String feedback;
        private String suggestion;
    }
}
