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
    private Data data;

    @lombok.Data
    public static class Data {
        private String question;
    }
}
