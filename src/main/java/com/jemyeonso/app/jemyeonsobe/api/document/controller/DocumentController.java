package com.jemyeonso.app.jemyeonsobe.api.document.controller;

import com.jemyeonso.app.jemyeonsobe.api.document.dto.ApiResponse;
import com.jemyeonso.app.jemyeonsobe.api.document.dto.DocumentResponse;
import com.jemyeonso.app.jemyeonsobe.api.document.service.DocumentService;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentAccessDeniedException;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.jemyeonso.app.jemyeonsobe.api.document.dto.DocumentListResponse;
import java.util.List;

@RestController
@RequestMapping("/api/backend/file")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "문서 보관함 관련 API입니다.")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/{document_id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(@PathVariable("document_id") Long documentId) {
        try {
            DocumentResponse document = documentService.getDocument(documentId);
            return ResponseEntity.ok(ApiResponse.success("파일 조회에 성공하였습니다.", document));
        } catch (DocumentNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("FILE_NOT_FOUND", "파일을 찾을 수 없습니다."));
        } catch (DocumentAccessDeniedException e) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("FILE_ACCESS_DENIED", "파일에 접근할 권한이 없습니다."));
        }
    }

    @DeleteMapping("/{document_id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable("document_id") Long documentId) {
        try {
            documentService.deleteDocument(documentId);
            return ResponseEntity.ok(ApiResponse.successDelete("파일이 삭제되었습니다."));
        } catch (DocumentNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("FILE_NOT_FOUND", "파일을 찾을 수 없습니다"));
        } catch (DocumentAccessDeniedException e) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("FILE_ACCESS_DENIED", "파일을 삭제할 권한이 없습니다."));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentListResponse>>> getDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 파라미터 검증
        if (page < 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_PARAMETER", "페이지는 0 이상이어야 합니다."));
        }

        if (size < 1 || size > 100) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_PARAMETER", "페이지 크기는 1 이상 100 이하여야 합니다."));
        }

        try {
            List<DocumentListResponse> documents = documentService.getDocumentsList(page, size);
            return ResponseEntity.ok(ApiResponse.successList("파일 목록 조회에 성공하였습니다.", documents));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR", "서버 오류가 발생했습니다."));
        }
    }

}