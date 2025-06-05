package com.jemyeonso.app.jemyeonsobe.api.interviews.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class QuestionDetailsDto {
    private Long questionId;
    private String questionText;
    private Integer questionNum;
    private String questionType;
    private AnswerDto answer;
    private FeedbackDto feedback;
    private List<AnalysisDto> analysis;
}
