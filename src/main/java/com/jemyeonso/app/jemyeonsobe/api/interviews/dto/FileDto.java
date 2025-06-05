package com.jemyeonso.app.jemyeonsobe.api.interviews.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class FileDto {
    private Long fileId;
    private String fileName;
    private String fileUrl;
}
