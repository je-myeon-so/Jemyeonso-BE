package com.jemyeonso.app.jemyeonsobe.api.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiServerRequestDto {
    private String fileUrl;
    private Long userId;
    private Long documentId;
    private String fileType;
}
