package com.jemyeonso.app.jemyeonsobe.api.document.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
public class FileValidationService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("application/pdf");
    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList("resume", "portfolio");

    public void validateFile(MultipartFile file, String documentType) {
        // 빈 파일 검증
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        // 파일 타입 검증 (MIME 타입)
        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("PDF 파일만 업로드 가능합니다.");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("PDF 파일만 업로드 가능합니다.");
        }

        // 문서 타입 검증
        if (!ALLOWED_DOCUMENT_TYPES.contains(documentType)) {
            throw new IllegalArgumentException("유효하지 않은 문서 타입입니다. (resume, portfolio만 허용)");
        }
    }
}
