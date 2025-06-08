package com.jemyeonso.app.jemyeonsobe.api.document.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class FileUploadResponseDto {
    private Long id;
    private String filename;
    private String originalFileName;
    private Long fileSize;
    private String link;
    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}
