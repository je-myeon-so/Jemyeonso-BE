package com.jemyeonso.app.jemyeonsobe.api.document.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jemyeonso.app.jemyeonsobe.api.document.controller.DocumentController;
import com.jemyeonso.app.jemyeonsobe.api.document.dto.DocumentRepositoryResponse;
import com.jemyeonso.app.jemyeonsobe.api.document.dto.DocumentResponse;
import com.jemyeonso.app.jemyeonsobe.api.document.dto.FileUploadResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.document.service.DocumentService;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentAccessDeniedException;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentNotFoundException;
import com.jemyeonso.app.jemyeonsobe.util.SecurityUtil;
import org.mockito.MockedStatic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@DisplayName("DocumentController 단위 테스트")
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentService documentService;

    private final Long TEST_USER_ID = 100L;

    @Nested
    @DisplayName("문서 상세 조회 API")
    class GetDocumentTest {

        @Test
        @WithMockUser(username = "100") // 인증된 사용자로 테스트
        @DisplayName("정상적인 문서 조회 - 성공")
        void getDocument_Success() throws Exception {
            // given
            Long documentId = 1L;
            DocumentResponse mockResponse = DocumentResponse.builder()
                    .documentId(documentId)
                    .userId(TEST_USER_ID)
                    .type("resume")
                    .filename("이력서.pdf")
                    .content("이력서 내용")
                    .createdAt(LocalDateTime.now())
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.getDocument(documentId, TEST_USER_ID)).willReturn(mockResponse);

                // when & then
                mockMvc.perform(get("/api/backend/file/{document_id}", documentId)
                                .with(csrf())) // CSRF 토큰 추가
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value("FILE_GET_SUCCESS"))
                        .andExpect(jsonPath("$.message").value("파일 조회에 성공하였습니다."))
                        .andExpect(jsonPath("$.data.documentId").value(documentId))
                        .andExpect(jsonPath("$.data.userId").value(TEST_USER_ID))
                        .andExpect(jsonPath("$.data.type").value("resume"))
                        .andExpect(jsonPath("$.data.filename").value("이력서.pdf"));
            }
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("존재하지 않는 문서 조회 - 404 에러")
        void getDocument_NotFound() throws Exception {
            // given
            Long documentId = 999L;

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.getDocument(documentId, TEST_USER_ID))
                        .willThrow(new DocumentNotFoundException("Document not found"));

                // when & then
                mockMvc.perform(get("/api/backend/file/{document_id}", documentId)
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("FILE_NOT_FOUND"))
                        .andExpect(jsonPath("$.message").value("파일을 찾을 수 없습니다."))
                        .andExpect(jsonPath("$.data").isEmpty());
            }
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("다른 사용자의 문서 접근 시도 - 403 에러")
        void getDocument_AccessDenied() throws Exception {
            // given
            Long documentId = 1L;

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.getDocument(documentId, TEST_USER_ID))
                        .willThrow(new DocumentAccessDeniedException("Access denied"));

                // when & then
                mockMvc.perform(get("/api/backend/file/{document_id}", documentId)
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.code").value("FILE_ACCESS_DENIED"))
                        .andExpect(jsonPath("$.message").value("파일에 접근할 권한이 없습니다."));
            }
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 접근 - 401 에러")
        void getDocument_Unauthorized() throws Exception {
            // given
            Long documentId = 1L;

            // when & then (인증 정보 없이 요청)
            mockMvc.perform(get("/api/backend/file/{document_id}", documentId))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("문서 삭제 API")
    class DeleteDocumentTest {

        @Test
        @WithMockUser(username = "100")
        @DisplayName("정상적인 문서 삭제 - 성공")
        void deleteDocument_Success() throws Exception {
            // given
            Long documentId = 1L;

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                doNothing().when(documentService).deleteDocument(documentId, TEST_USER_ID);

                // when & then
                mockMvc.perform(delete("/api/backend/file/{document_id}", documentId)
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value("FILE_GET_SUCCESS"))
                        .andExpect(jsonPath("$.message").value("파일이 삭제되었습니다."))
                        .andExpect(jsonPath("$.data").isEmpty());
            }
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("존재하지 않는 문서 삭제 시도 - 404 에러")
        void deleteDocument_NotFound() throws Exception {
            // given
            Long documentId = 999L;

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                doThrow(new DocumentNotFoundException("Document not found"))
                        .when(documentService).deleteDocument(documentId, TEST_USER_ID);

                // when & then
                mockMvc.perform(delete("/api/backend/file/{document_id}", documentId)
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("FILE_NOT_FOUND"))
                        .andExpect(jsonPath("$.message").value("파일을 찾을 수 없습니다."));
            }
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("다른 사용자의 문서 삭제 시도 - 403 에러")
        void deleteDocument_AccessDenied() throws Exception {
            // given
            Long documentId = 1L;

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                doThrow(new DocumentAccessDeniedException("Access denied"))
                        .when(documentService).deleteDocument(documentId, TEST_USER_ID);

                // when & then
                mockMvc.perform(delete("/api/backend/file/{document_id}", documentId)
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.code").value("FILE_ACCESS_DENIED"))
                        .andExpect(jsonPath("$.message").value("파일을 삭제할 권한이 없습니다."));
            }
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 삭제 시도 - 403 에러 (CSRF 필요)")
        void deleteDocument_Unauthorized() throws Exception {
            // given
            Long documentId = 1L;

            // when & then
            // 인증되지 않은 상태에서 DELETE 요청 시 CSRF 때문에 403 발생
            mockMvc.perform(delete("/api/backend/file/{document_id}", documentId))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 401이 아닌 403 예상
        }
    }

    @Nested
    @DisplayName("문서 목록 조회 API")
    class GetDocumentsTest {

        @Test
        @WithMockUser(username = "100")
        @DisplayName("기본 페이지네이션으로 목록 조회 - 성공")
        void getDocuments_DefaultPagination_Success() throws Exception {
            // given
            DocumentRepositoryResponse mockResponse = DocumentRepositoryResponse.builder()
                    .documents(Arrays.asList(
                            DocumentResponse.builder()
                                    .documentId(1L)
                                    .userId(TEST_USER_ID)
                                    .type("resume")
                                    .filename("이력서.pdf")
                                    .createdAt(LocalDateTime.now())
                                    .build(),
                            DocumentResponse.builder()
                                    .documentId(2L)
                                    .userId(TEST_USER_ID)
                                    .type("portfolio")
                                    .filename("포트폴리오.pdf")
                                    .createdAt(LocalDateTime.now().minusDays(1))
                                    .build()
                    ))
                    .page(0)
                    .totalPages(1)
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.getDocumentsList(0, 10, TEST_USER_ID)).willReturn(mockResponse);

                // when & then
                mockMvc.perform(get("/api/backend/file")
                                .param("page", "0")
                                .param("size", "10")
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value("FILE_GET_SUCCESS"))
                        .andExpect(jsonPath("$.message").value("파일 목록 조회에 성공하였습니다."))
                        .andExpect(jsonPath("$.data.documents").isArray())
                        .andExpect(jsonPath("$.data.documents.length()").value(2))
                        .andExpect(jsonPath("$.data.page").value(0))
                        .andExpect(jsonPath("$.data.totalPages").value(1));
            }
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("빈 목록 조회 - 성공")
        void getDocuments_EmptyList_Success() throws Exception {
            // given
            DocumentRepositoryResponse emptyResponse = DocumentRepositoryResponse.builder()
                    .documents(Arrays.asList())
                    .page(0)
                    .totalPages(1)
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.getDocumentsList(0, 10, TEST_USER_ID)).willReturn(emptyResponse);

                // when & then
                mockMvc.perform(get("/api/backend/file")
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.documents").isArray())
                        .andExpect(jsonPath("$.data.documents.length()").value(0));
            }
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("잘못된 페이지 번호 - 400 에러")
        void getDocuments_InvalidPageNumber() throws Exception {
            // when & then
            mockMvc.perform(get("/api/backend/file")
                            .param("page", "-1")
                            .param("size", "10")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("PAGINATION_INVALID_PARAMETER"))
                    .andExpect(jsonPath("$.message").value("유효하지 않은 페이지네이션 파라미터입니다."));
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("잘못된 페이지 크기 - 400 에러")
        void getDocuments_InvalidPageSize() throws Exception {
            // when & then
            mockMvc.perform(get("/api/backend/file")
                            .param("page", "0")
                            .param("size", "101")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("PAGINATION_INVALID_PARAMETER"))
                    .andExpect(jsonPath("$.message").value("유효하지 않은 페이지네이션 파라미터입니다."));
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("서버 내부 오류 - 500 에러")
        void getDocuments_InternalError() throws Exception {
            // given
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.getDocumentsList(0, 10, TEST_USER_ID))
                        .willThrow(new RuntimeException("Database connection error"));

                // when & then
                mockMvc.perform(get("/api/backend/file")
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                        .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다."));
            }
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 접근 - 401 에러")
        void getDocuments_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/backend/file"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("파일 업로드 API")
    class UploadFileTest {

        @Test
        @WithMockUser(username = "100")
        @DisplayName("정상적인 PDF 파일 업로드 (resume) - 성공")
        void uploadFile_Resume_Success() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "이력서.pdf",
                    "application/pdf",
                    "PDF content".getBytes()
            );

            FileUploadResponseDto mockResponse = FileUploadResponseDto.builder()
                    .id(1L)
                    .filename("이력서.pdf")
                    .originalFileName("이력서.pdf")
                    .fileSize(1024L)
                    .link("https://s3.amazonaws.com/documents/resumes/이력서.pdf")
                    .type("resume")
                    .createdAt(LocalDateTime.now())
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.uploadFile(any(), eq("resume"), eq(TEST_USER_ID)))
                        .willReturn(mockResponse);

                // when & then
                mockMvc.perform(multipart("/api/backend/file")
                                .file(file)
                                .param("type", "resume")
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value("FILE_GET_SUCCESS"))
                        .andExpect(jsonPath("$.message").value("파일 업로드에 성공하였습니다."))
                        .andExpect(jsonPath("$.data.id").value(1L))
                        .andExpect(jsonPath("$.data.filename").value("이력서.pdf"))
                        .andExpect(jsonPath("$.data.type").value("resume"));
            }
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("정상적인 PDF 파일 업로드 (portfolio) - 성공")
        void uploadFile_Portfolio_Success() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "포트폴리오.pdf",
                    "application/pdf",
                    "PDF content".getBytes()
            );

            FileUploadResponseDto mockResponse = FileUploadResponseDto.builder()
                    .id(2L)
                    .filename("포트폴리오.pdf")
                    .originalFileName("포트폴리오.pdf")
                    .fileSize(2048L)
                    .link("https://s3.amazonaws.com/documents/portfolios/포트폴리오.pdf")
                    .type("portfolio")
                    .createdAt(LocalDateTime.now())
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.uploadFile(any(), eq("portfolio"), eq(TEST_USER_ID)))
                        .willReturn(mockResponse);

                // when & then
                mockMvc.perform(multipart("/api/backend/file")
                                .file(file)
                                .param("type", "portfolio")
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value("FILE_GET_SUCCESS"))
                        .andExpect(jsonPath("$.message").value("파일 업로드에 성공하였습니다."))
                        .andExpect(jsonPath("$.data.id").value(2L))
                        .andExpect(jsonPath("$.data.filename").value("포트폴리오.pdf"))
                        .andExpect(jsonPath("$.data.type").value("portfolio"));
            }
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("빈 파일 업로드 - 400 에러")
        void uploadFile_EmptyFile_BadRequest() throws Exception {
            // given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    "empty.pdf",
                    "application/pdf",
                    new byte[0]
            );

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.uploadFile(any(), eq("resume"), eq(TEST_USER_ID)))
                        .willThrow(new IllegalArgumentException("빈 파일은 업로드할 수 없습니다."));

                // when & then
                mockMvc.perform(multipart("/api/backend/file")
                                .file(emptyFile)
                                .param("type", "resume")
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("EMPTY_FILE"))
                        .andExpect(jsonPath("$.message").value("빈 파일은 업로드할 수 없습니다."));
            }
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("파일 크기 초과 - 400 에러")
        void uploadFile_FileSizeExceeded_BadRequest() throws Exception {
            // given
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file",
                    "large.pdf",
                    "application/pdf",
                    new byte[1024]
            );

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.uploadFile(any(), eq("resume"), eq(TEST_USER_ID)))
                        .willThrow(new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다."));

                // when & then
                mockMvc.perform(multipart("/api/backend/file")
                                .file(largeFile)
                                .param("type", "resume")
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("FILE_SIZE_EXCEEDED"))
                        .andExpect(jsonPath("$.message").value("파일 크기는 10MB를 초과할 수 없습니다."));
            }
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("잘못된 파일 타입 - 400 에러")
        void uploadFile_InvalidFileType_BadRequest() throws Exception {
            // given
            MockMultipartFile txtFile = new MockMultipartFile(
                    "file",
                    "document.txt",
                    "text/plain",
                    "Text content".getBytes()
            );

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.uploadFile(any(), eq("resume"), eq(TEST_USER_ID)))
                        .willThrow(new IllegalArgumentException("PDF 파일만 업로드 가능합니다."));

                // when & then
                mockMvc.perform(multipart("/api/backend/file")
                                .file(txtFile)
                                .param("type", "resume")
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("INVALID_FILE_TYPE"))
                        .andExpect(jsonPath("$.message").value("PDF 파일만 업로드 가능합니다."));
            }
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("잘못된 문서 타입 - 400 에러")
        void uploadFile_InvalidDocumentType_BadRequest() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "document.pdf",
                    "application/pdf",
                    "PDF content".getBytes()
            );

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.uploadFile(any(), eq("invalid"), eq(TEST_USER_ID)))
                        .willThrow(new IllegalArgumentException("유효하지 않은 문서 타입입니다. (resume, portfolio만 허용)"));

                // when & then
                mockMvc.perform(multipart("/api/backend/file")
                                .file(file)
                                .param("type", "invalid")
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("INVALID_DOCUMENT_TYPE"))
                        .andExpect(jsonPath("$.message").value("유효하지 않은 문서 타입입니다. (resume, portfolio만 허용)"));
            }
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("S3 업로드 실패 - 500 에러")
        void uploadFile_S3UploadFailed_InternalServerError() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "document.pdf",
                    "application/pdf",
                    "PDF content".getBytes()
            );

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.uploadFile(any(), eq("resume"), eq(TEST_USER_ID)))
                        .willThrow(new RuntimeException("S3 파일 업로드에 실패했습니다."));

                // when & then
                mockMvc.perform(multipart("/api/backend/file")
                                .file(file)
                                .param("type", "resume")
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.code").value("S3_UPLOAD_FAILED"))
                        .andExpect(jsonPath("$.message").value("파일 업로드 중 오류가 발생했습니다."));
            }
        }

        @Test
        @WithMockUser(username = "100")
        @DisplayName("데이터베이스 저장 실패 - 500 에러")
        void uploadFile_DatabaseError_InternalServerError() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "document.pdf",
                    "application/pdf",
                    "PDF content".getBytes()
            );

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(TEST_USER_ID);
                given(documentService.uploadFile(any(), eq("resume"), eq(TEST_USER_ID)))
                        .willThrow(new RuntimeException("데이터베이스 연결 오류"));

                // when & then
                mockMvc.perform(multipart("/api/backend/file")
                                .file(file)
                                .param("type", "resume")
                                .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                        .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
            }
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 업로드 - 403 에러 (CSRF 필요)")
        void uploadFile_Unauthorized() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "document.pdf",
                    "application/pdf",
                    "PDF content".getBytes()
            );

            // when & then
            // 인증되지 않은 상태에서 POST 요청 시 CSRF 때문에 403 발생
            mockMvc.perform(multipart("/api/backend/file")
                            .file(file)
                            .param("type", "resume"))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 401이 아닌 403 예상
        }
    }
}
