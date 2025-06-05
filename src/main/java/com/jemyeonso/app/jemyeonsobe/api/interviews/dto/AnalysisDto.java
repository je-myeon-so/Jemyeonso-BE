package com.jemyeonso.app.jemyeonsobe.api.interviews.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class AnalysisDto {
    private String errorText;
    private String errorType;
    private String feedbackText;
    private String suggestion;
}
