package com.jemyeonso.app.jemyeonsobe.api.document.controller;

import com.jemyeonso.app.jemyeonsobe.api.document.dto.DocumentResponse;
import com.jemyeonso.app.jemyeonsobe.common.enums.*;
import com.jemyeonso.app.jemyeonsobe.api.document.service.DocumentService;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentAccessDeniedException;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable("document_id") Long documentId) {
        try {
            documentService.deleteDocument(documentId);
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
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ApiResponseCode.PAGINATION_INVALID_PARAMETER));
        }

        try {
            List<DocumentResponse> documents = documentService.getDocumentsList(page, size);
            return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.FILE_LIST_SUCCESS, documents));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(ApiResponseCode.INTERNAL_ERROR));
        }
    }
}