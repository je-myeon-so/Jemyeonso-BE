package com.jemyeonso.app.jemyeonsobe.api.document.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequestDto {
    private MultipartFile file;
    private String type; // "resume" 또는 "portfolio"
}
