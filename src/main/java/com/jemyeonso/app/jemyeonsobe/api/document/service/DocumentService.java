package com.jemyeonso.app.jemyeonsobe.api.document.service;

import com.jemyeonso.app.jemyeonsobe.api.document.dto.DocumentResponse;
import com.jemyeonso.app.jemyeonsobe.api.document.entity.Document;
import com.jemyeonso.app.jemyeonsobe.api.document.repository.DocumentRepository;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentAccessDeniedException;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;

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

    public List<DocumentResponse> getDocumentsList(int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 현재 로그인한 유저의 문서만 조회
        Page<Document> documentPage = documentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return documentPage.getContent().stream()
                .map(DocumentResponse::from)
                .collect(Collectors.toList());
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
}
