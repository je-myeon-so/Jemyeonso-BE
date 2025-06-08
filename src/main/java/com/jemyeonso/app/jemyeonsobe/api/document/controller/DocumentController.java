package com.jemyeonso.app.jemyeonsobe.api.document.controller;

import com.jemyeonso.app.jemyeonsobe.api.document.dto.DocumentResponse;
import com.jemyeonso.app.jemyeonsobe.api.document.dto.ApiResponse;
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
import com.jemyeonso.app.jemyeonsobe.api.document.dto.FileUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

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

        // 현재 로그인한 유저 ID 추출
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            DocumentResponse document = documentService.getDocument(documentId, userId);
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
    @Operation(summary = "문서 삭제", description = "문서 ID로 특정 문서를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @Parameter(description = "문서 ID") @PathVariable("document_id") Long documentId) {

        // 현재 로그인한 유저 ID 추출
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            documentService.deleteDocument(documentId, userId);
            return ResponseEntity.ok(ApiResponse.success("파일이 삭제되었습니다.", null));
        } catch (DocumentNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("FILE_NOT_FOUND", "파일을 찾을 수 없습니다."));
        } catch (DocumentAccessDeniedException e) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("FILE_ACCESS_DENIED", "파일을 삭제할 권한이 없습니다."));
        }
    }

    @GetMapping
    @Operation(summary = "문서 목록 조회", description = "현재 로그인한 사용자의 문서 목록을 페이지네이션으로 조회합니다.")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocuments(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (1-100)") @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("PAGINATION_INVALID_PARAMETER", "유효하지 않은 페이지네이션 파라미터입니다."));
        }

        // 현재 로그인한 유저 ID 추출
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            List<DocumentResponse> documents = documentService.getDocumentsList(page, size, userId);
            return ResponseEntity.ok(ApiResponse.success("파일 목록 조회에 성공하였습니다.", documents));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR", "서버 오류가 발생했습니다."));
        }
    }

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "파일 업로드", description = "PDF 파일을 업로드하고 S3에 저장합니다.")
    public ResponseEntity<ApiResponse<FileUploadResponseDto>> uploadFile(
            @Parameter(description = "업로드할 PDF 파일") @RequestParam("file") MultipartFile file,
            @Parameter(description = "문서 타입 (resume 또는 portfolio)") @RequestParam("type") String type) {

        // 현재 로그인한 유저 ID 추출
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            FileUploadResponseDto response = documentService.uploadFile(file, type, userId);
            return ResponseEntity.ok(ApiResponse.success("파일 업로드에 성공하였습니다.", response));

        } catch (IllegalArgumentException e) {
            // 파일 검증 실패
            String errorCode = getErrorCodeFromMessage(e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(errorCode, e.getMessage()));

        } catch (RuntimeException e) {
            // S3 업로드 실패 등
            if (e.getMessage().contains("S3")) {
                return ResponseEntity.status(500)
                        .body(ApiResponse.error("S3_UPLOAD_FAILED", "파일 업로드 중 오류가 발생했습니다."));
            }
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    private String getErrorCodeFromMessage(String message) {
        if (message.contains("빈 파일")) {
            return "EMPTY_FILE";
        } else if (message.contains("파일 크기")) {
            return "FILE_SIZE_EXCEEDED";
        } else if (message.contains("PDF")) {
            return "INVALID_FILE_TYPE";
        } else if (message.contains("문서 타입")) {
            return "INVALID_DOCUMENT_TYPE";
        }
        return "BAD_REQUEST";
    }
}
