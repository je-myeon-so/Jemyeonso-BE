package com.jemyeonso.app.jemyeonsobe.api.document.controller;

import com.jemyeonso.app.jemyeonsobe.api.document.dto.DocumentResponse;
import com.jemyeonso.app.jemyeonsobe.common.enums.*;
import com.jemyeonso.app.jemyeonsobe.api.document.service.DocumentService;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentAccessDeniedException;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backend/file")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "문서 보관함 관련 API입니다.")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/{document_id}")
    @Operation(summary = "문서 상세 조회", description = "문서 ID로 특정 문서의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
            @Parameter(description = "문서 ID") @PathVariable("document_id") Long documentId) {

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            DocumentResponse document = documentService.getDocument(documentId, userId);
            return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.FILE_GET_SUCCESS, document));
        } catch (DocumentNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(ApiResponseCode.FILE_NOT_FOUND));
        } catch (DocumentAccessDeniedException e) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(ApiResponseCode.FILE_ACCESS_DENIED));
        }
    }

    @DeleteMapping("/{document_id}")
    @Operation(summary = "문서 삭제", description = "문서 ID로 특정 문서를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @Parameter(description = "문서 ID") @PathVariable("document_id") Long documentId) {

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            documentService.deleteDocument(documentId, userId);
            return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.FILE_DELETE_SUCCESS, "파일이 삭제되었습니다."));
        } catch (DocumentNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(ApiResponseCode.FILE_NOT_FOUND));
        } catch (DocumentAccessDeniedException e) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(ApiResponseCode.FILE_ACCESS_DENIED, "파일을 삭제할 권한이 없습니다."));
        }
    }

    @GetMapping
    @Operation(summary = "문서 목록 조회", description = "현재 로그인한 사용자의 문서 목록을 페이지네이션으로 조회합니다.")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocuments(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (1-100)") @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ApiResponseCode.PAGINATION_INVALID_PARAMETER));
        }

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            List<DocumentResponse> documents = documentService.getDocumentsList(page, size, userId);
            return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.FILE_LIST_SUCCESS, documents));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(ApiResponseCode.INTERNAL_ERROR));
        }
    }
}
