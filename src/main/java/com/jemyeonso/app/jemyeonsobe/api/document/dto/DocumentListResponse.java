package com.jemyeonso.app.jemyeonsobe.api.document.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jemyeonso.app.jemyeonsobe.api.document.entity.Document;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentListResponse {

    private Long documentId;
    private Long userId;
    private String type;
    private String filename;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    public static DocumentListResponse from(Document document) {
        return DocumentListResponse.builder()
                .documentId(document.getId())
                .userId(document.getUserId())
                .type(document.getType())
                .filename(document.getFilename())
                .createdAt(document.getCreatedAt())
                .build();
    }
}