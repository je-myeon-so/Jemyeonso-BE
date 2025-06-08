package com.jemyeonso.app.jemyeonsobe.api.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiServerRequestDto {
    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("document_id")
    private Long documentId;

    @JsonProperty("file_type")
    private String fileType;
}
