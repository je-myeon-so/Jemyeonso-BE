package com.jemyeonso.app.jemyeonsobe.api.document.service;

import com.jemyeonso.app.jemyeonsobe.api.document.dto.DocumentRepositoryResponse;
import com.jemyeonso.app.jemyeonsobe.api.document.dto.DocumentResponse;
import com.jemyeonso.app.jemyeonsobe.api.document.dto.FileUploadResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.document.entity.Document;
import com.jemyeonso.app.jemyeonsobe.api.document.repository.DocumentRepository;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentAccessDeniedException;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final S3FileService s3FileService;
    private final AiServerService aiServerService;
    private final FileValidationService fileValidationService;

    // 유저 검증이 있는 새로운 메서드
    public DocumentResponse getDocument(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with id: " + documentId));

        // 유저 검증: 문서 소유자와 요청한 유저가 다르면 접근 거부
        if (!document.getUserId().equals(userId)) {
            throw new DocumentAccessDeniedException("Access denied to document with id: " + documentId);
        }

        return DocumentResponse.from(document);
    }

    @Transactional
    public void deleteDocument(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with id: " + documentId));

        // 유저 검증: 문서 소유자와 요청한 유저가 다르면 삭제 거부
        if (!document.getUserId().equals(userId)) {
            throw new DocumentAccessDeniedException("Access denied to delete document with id: " + documentId);
        }

        documentRepository.delete(document);
    }

    public DocumentRepositoryResponse getDocumentsList(int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 현재 로그인한 유저의 문서만 조회
        Page<Document> documentPage = documentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<DocumentResponse> documents = documentPage.getContent().stream()
                .map(DocumentResponse::from)
                .collect(Collectors.toList());

        // Page<DocumentResponse> 형태로 변환하여 from 메서드 사용
        Page<DocumentResponse> documentResponsePage = new PageImpl<>(
                documents,
                pageable,
                documentPage.getTotalElements()
        );

        return DocumentRepositoryResponse.from(documentResponsePage);
    }

    // 기존 메서드들 (하위 호환성을 위해 유지)
    public DocumentResponse getDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with id: " + documentId));

        return DocumentResponse.from(document);
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with id: " + documentId));

        documentRepository.delete(document);
    }

    public List<DocumentResponse> getDocumentsList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Document> documentPage = documentRepository.findAll(pageable);

        return documentPage.getContent().stream()
                .map(DocumentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public FileUploadResponseDto uploadFile(MultipartFile file, String documentType, Long userId) {
        try {
            // 1. 파일 검증
            fileValidationService.validateFile(file, documentType);

            // 2. S3에 파일 업로드
            String s3Url = s3FileService.uploadFile(file, userId, documentType);

            // 3. 데이터베이스에 문서 정보 저장
            Document document = Document.builder()
                    .userId(userId)
                    .type(documentType)
                    .filename(extractFileNameFromUrl(s3Url))
                    .content("") // AI 서버에서 처리 후 업데이트
                    .link(s3Url)
                    .build();

            Document savedDocument = documentRepository.save(document);

            // 4. AI 서버에 비동기 요청 전송
            aiServerService.sendDocumentToAiServer(s3Url, userId, savedDocument.getId(), documentType);

            // 5. 응답 데이터 생성
            return FileUploadResponseDto.builder()
                    .id(savedDocument.getId())
                    .filename(savedDocument.getFilename())
                    .originalFileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .link(savedDocument.getLink())
                    .type(savedDocument.getType())
                    .createdAt(savedDocument.getCreatedAt())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    private String extractFileNameFromUrl(String s3Url) {
        // URL에서 파일명 추출
        String[] parts = s3Url.split("/");
        return parts[parts.length - 1];
    }
}
