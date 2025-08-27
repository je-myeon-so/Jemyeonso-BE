package com.jemyeonso.app.jemyeonsobe.api.interviews.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class
InterviewRequestDto {
    private String interviewTitle;
    private String questionCategory;
    private String questionLevel;
    private String jobType;
    private Long documentId;
}
