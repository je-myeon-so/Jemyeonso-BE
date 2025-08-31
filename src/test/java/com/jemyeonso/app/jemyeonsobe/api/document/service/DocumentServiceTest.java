package com.jemyeonso.app.jemyeonsobe.api.document.service;

import com.jemyeonso.app.jemyeonsobe.api.document.dto.DocumentRepositoryResponse;
import com.jemyeonso.app.jemyeonsobe.api.document.dto.DocumentResponse;
import com.jemyeonso.app.jemyeonsobe.api.document.dto.FileUploadResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.document.entity.Document;
import com.jemyeonso.app.jemyeonsobe.api.document.repository.DocumentRepository;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentAccessDeniedException;
import com.jemyeonso.app.jemyeonsobe.common.exception.DocumentNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentService 단위 테스트")
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private S3FileService s3FileService;

    @Mock
    private AiServerService aiServerService;

    @Mock
    private FileValidationService fileValidationService;

    @InjectMocks
    private DocumentService documentService;

    @Nested
    @DisplayName("문서 상세 조회")
    class GetDocumentTest {

        @Test
        @DisplayName("정상적인 문서 조회 - 성공")
        void getDocument_Success() {
            // given
            Long documentId = 1L;
            Long userId = 100L;
            Document mockDocument = Document.builder()
                    .id(documentId)
                    .userId(userId)
                    .type("resume")
                    .filename("이력서.pdf")
                    .content("이력서 내용")
                    .link("https://s3.amazonaws.com/file.pdf")
                    .createdAt(LocalDateTime.now())
                    .build();

            given(documentRepository.findById(documentId))
                    .willReturn(Optional.of(mockDocument));

            // when
            DocumentResponse result = documentService.getDocument(documentId, userId);

            // then
            assertThat(result.getDocumentId()).isEqualTo(documentId);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getType()).isEqualTo("resume");
            assertThat(result.getFilename()).isEqualTo("이력서.pdf");
        }

        @Test
        @DisplayName("존재하지 않는 문서 조회 - 실패")
        void getDocument_NotFound() {
            // given
            Long documentId = 999L;
            Long userId = 100L;

            given(documentRepository.findById(documentId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> documentService.getDocument(documentId, userId))
                    .isInstanceOf(DocumentNotFoundException.class)
                    .hasMessage("Document not found with id: " + documentId);
        }

        @Test
        @DisplayName("다른 사용자의 문서 접근 시도 - 실패")
        void getDocument_AccessDenied() {
            // given
            Long documentId = 1L;
            Long ownerId = 100L;
            Long requestUserId = 200L; // 다른 사용자

            Document mockDocument = Document.builder()
                    .id(documentId)
                    .userId(ownerId)
                    .type("resume")
                    .filename("이력서.pdf")
                    .build();

            given(documentRepository.findById(documentId))
                    .willReturn(Optional.of(mockDocument));

            // when & then
            assertThatThrownBy(() -> documentService.getDocument(documentId, requestUserId))
                    .isInstanceOf(DocumentAccessDeniedException.class)
                    .hasMessage("Access denied to document with id: " + documentId);
        }
    }

    @Nested
    @DisplayName("문서 삭제")
    class DeleteDocumentTest {

        @Test
        @DisplayName("정상적인 문서 삭제 - 성공")
        void deleteDocument_Success() {
            // given
            Long documentId = 1L;
            Long userId = 100L;
            Document mockDocument = Document.builder()
                    .id(documentId)
                    .userId(userId)
                    .type("resume")
                    .filename("이력서.pdf")
                    .deletedAt(null)
                    .build();

            given(documentRepository.findByIdAndDeletedAtIsNull(documentId))
                    .willReturn(Optional.of(mockDocument));

            // when
            documentService.deleteDocument(documentId, userId);

            // then
            assertThat(mockDocument.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 문서 삭제 시도 - 실패")
        void deleteDocument_NotFound() {
            // given
            Long documentId = 999L;
            Long userId = 100L;

            given(documentRepository.findByIdAndDeletedAtIsNull(documentId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> documentService.deleteDocument(documentId, userId))
                    .isInstanceOf(DocumentNotFoundException.class)
                    .hasMessage("Document not found with id: " + documentId);
        }

        @Test
        @DisplayName("다른 사용자의 문서 삭제 시도 - 실패")
        void deleteDocument_AccessDenied() {
            // given
            Long documentId = 1L;
            Long ownerId = 100L;
            Long requestUserId = 200L;

            Document mockDocument = Document.builder()
                    .id(documentId)
                    .userId(ownerId)
                    .build();

            given(documentRepository.findByIdAndDeletedAtIsNull(documentId))
                    .willReturn(Optional.of(mockDocument));

            // when & then
            assertThatThrownBy(() -> documentService.deleteDocument(documentId, requestUserId))
                    .isInstanceOf(DocumentAccessDeniedException.class)
                    .hasMessage("Access denied to delete document with id: " + documentId);
        }
    }

    @Nested
    @DisplayName("문서 목록 조회")
    class GetDocumentsListTest {

        @Test
        @DisplayName("기본 페이지네이션으로 목록 조회 - 성공")
        void getDocumentsList_DefaultPagination_Success() {
            // given
            int page = 0;
            int size = 10;
            Long userId = 100L;

            List<Document> mockDocuments = Arrays.asList(
                    Document.builder()
                            .id(1L)
                            .userId(userId)
                            .type("resume")
                            .filename("이력서1.pdf")
                            .createdAt(LocalDateTime.now())
                            .build(),
                    Document.builder()
                            .id(2L)
                            .userId(userId)
                            .type("portfolio")
                            .filename("포트폴리오.pdf")
                            .createdAt(LocalDateTime.now().minusDays(1))
                            .build()
            );

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Document> mockPage = new PageImpl<>(mockDocuments, pageable, mockDocuments.size());

            given(documentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
                    .willReturn(mockPage);

            // when
            DocumentRepositoryResponse result = documentService.getDocumentsList(page, size, userId);

            // then
            assertThat(result.getDocuments()).hasSize(2);
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getDocuments().get(0).getDocumentId()).isEqualTo(1L);
            assertThat(result.getDocuments().get(1).getDocumentId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("빈 목록 조회 - 성공")
        void getDocumentsList_EmptyList_Success() {
            // given
            int page = 0;
            int size = 10;
            Long userId = 100L;

            List<Document> emptyDocuments = Arrays.asList();
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Document> emptyPage = new PageImpl<>(emptyDocuments, pageable, 0);

            given(documentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
                    .willReturn(emptyPage);

            // when
            DocumentRepositoryResponse result = documentService.getDocumentsList(page, size, userId);

            // then
            assertThat(result.getDocuments()).isEmpty();
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getTotalPages()).isIn(0, 1);
        }
    }

    @Nested
    @DisplayName("파일 업로드")
    class UploadFileTest {

        @Mock
        private MultipartFile mockFile;

        @Test
        @DisplayName("정상적인 PDF 파일 업로드 (resume) - 성공")
        void uploadFile_Resume_Success() throws Exception {
            // given
            String documentType = "resume";
            Long userId = 100L;
            String s3Url = "https://s3.amazonaws.com/documents/resumes/이력서.pdf";

            given(mockFile.getOriginalFilename()).willReturn("이력서.pdf");
            given(mockFile.getSize()).willReturn(1024L);
            given(s3FileService.uploadFile(mockFile, userId, documentType)).willReturn(s3Url);

            Document savedDocument = Document.builder()
                    .id(1L)
                    .userId(userId)
                    .type(documentType)
                    .filename("이력서.pdf")
                    .content("")
                    .link(s3Url)
                    .createdAt(LocalDateTime.now())
                    .build();

            given(documentRepository.save(any(Document.class))).willReturn(savedDocument);

            // when
            FileUploadResponseDto result = documentService.uploadFile(mockFile, documentType, userId);

            // then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFilename()).isEqualTo("이력서.pdf");
            assertThat(result.getOriginalFileName()).isEqualTo("이력서.pdf");
            assertThat(result.getFileSize()).isEqualTo(1024L);
            assertThat(result.getLink()).isEqualTo(s3Url);
            assertThat(result.getType()).isEqualTo(documentType);

            // AI 서버 호출 확인
            verify(aiServerService).sendDocumentToAiServer(s3Url, userId, 1L, documentType);
        }

        @Test
        @DisplayName("파일 검증 실패 - 빈 파일")
        void uploadFile_EmptyFile_Fail() {
            // given
            String documentType = "resume";
            Long userId = 100L;

            doThrow(new IllegalArgumentException("빈 파일은 업로드할 수 없습니다."))
                    .when(fileValidationService).validateFile(mockFile, documentType);

            // when & then
            assertThatThrownBy(() -> documentService.uploadFile(mockFile, documentType, userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("파일 업로드 중 오류가 발생했습니다.");
        }

        @Test
        @DisplayName("S3 업로드 실패")
        void uploadFile_S3UploadFail() throws Exception {
            // given
            String documentType = "resume";
            Long userId = 100L;

            given(s3FileService.uploadFile(mockFile, userId, documentType))
                    .willThrow(new RuntimeException("S3 파일 업로드에 실패했습니다."));

            // when & then
            assertThatThrownBy(() -> documentService.uploadFile(mockFile, documentType, userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("파일 업로드 중 오류가 발생했습니다.");
        }
    }
}
