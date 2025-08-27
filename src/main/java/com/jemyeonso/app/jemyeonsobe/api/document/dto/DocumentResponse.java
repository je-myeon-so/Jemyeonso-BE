package com.jemyeonso.app.jemyeonsobe.api.document.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jemyeonso.app.jemyeonsobe.api.document.entity.Document;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentResponse {

    private Long documentId;
    private Long userId;
    private String type;
    private String filename;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    public static DocumentResponse from(Document document) {
        return DocumentResponse.builder()
                .documentId(document.getId())
                .userId(document.getUserId())
                .type(document.getType())
                .filename(document.getFilename())
                .content(document.getContent())
                .createdAt(document.getCreatedAt())
                .build();
    }
}