package com.jemyeonso.app.jemyeonsobe.api.interviews.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Interview;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class InterviewListResponse {

    private Long interviewId;
    private Long documentId;
    private Long userId;
    private String title;
    private String questionType;
    private String questionLevel;
    private String jobtype;  // 추가된 필드

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    private DocumentInfo document;

    public static InterviewListResponse from(Interview interview) {
        return InterviewListResponse.builder()
                .interviewId(interview.getId())
                .documentId(interview.getDocumentId())
                .userId(interview.getUserId())
                .title(interview.getTitle())
                .questionType(interview.getQuestionCategory().name())
                .questionLevel(interview.getQuestionLevel().name())
                .jobtype(interview.getJobtype())  // 추가된 매핑
                .createdAt(interview.getCreatedAt())
                .document(DocumentInfo.from(interview.getDocument()))
                .build();
    }

    @Getter
    @Builder
    public static class DocumentInfo {
        private Long documentId;
        private String type;
        private String filename;

        public static DocumentInfo from(com.jemyeonso.app.jemyeonsobe.api.document.entity.Document document) {
            if (document == null) return null;

            return DocumentInfo.builder()
                    .documentId(document.getId())
                    .type(document.getType())
                    .filename(document.getFilename())
                    .build();
        }
    }
}