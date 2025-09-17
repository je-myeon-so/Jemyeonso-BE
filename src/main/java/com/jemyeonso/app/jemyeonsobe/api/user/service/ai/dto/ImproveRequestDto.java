package com.jemyeonso.app.jemyeonsobe.api.user.service.ai.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImproveRequestDto {
    private Long interviewId;
    private String jobType;
    private List<QaItem> qaList;

    @Getter
    @Builder
    public class QaItem {
        private String question;
        private String answer;
    }
}
