package com.jemyeonso.app.jemyeonsobe.api.document.repository;

import com.jemyeonso.app.jemyeonsobe.api.document.entity.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("DocumentRepository 테스트")
class DocumentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DocumentRepository documentRepository;

    @Nested
    @DisplayName("findByIdAndDeletedAtIsNull 테스트")
    class FindByIdAndDeletedAtIsNullTest {

        @Test
        @DisplayName("삭제되지 않은 문서 조회 - 성공")
        void findByIdAndDeletedAtIsNull_NotDeleted_Success() {
            // given
            Document document = Document.builder()
                    .userId(100L)
                    .type("resume")
                    .filename("이력서.pdf")
                    .content("이력서 내용")
                    .link("https://s3.amazonaws.com/file.pdf")
                    .deletedAt(null)
                    .build();

            Document savedDocument = entityManager.persistAndFlush(document);

            // when
            Optional<Document> result = documentRepository.findByIdAndDeletedAtIsNull(savedDocument.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(savedDocument.getId());
            assertThat(result.get().getFilename()).isEqualTo("이력서.pdf");
            assertThat(result.get().getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("삭제된 문서 조회 - 빈 결과")
        void findByIdAndDeletedAtIsNull_Deleted_Empty() {
            // given
            Document document = Document.builder()
                    .userId(100L)
                    .type("resume")
                    .filename("삭제된_이력서.pdf")
                    .content("이력서 내용")
                    .link("https://s3.amazonaws.com/file.pdf")
                    .deletedAt(LocalDateTime.now())
                    .build();

            Document savedDocument = entityManager.persistAndFlush(document);

            // when
            Optional<Document> result = documentRepository.findByIdAndDeletedAtIsNull(savedDocument.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 문서 조회 - 빈 결과")
        void findByIdAndDeletedAtIsNull_NotExists_Empty() {
            // given
            Long nonExistentId = 999L;

            // when
            Optional<Document> result = documentRepository.findByIdAndDeletedAtIsNull(nonExistentId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserIdOrderByCreatedAtDesc 테스트")
    class FindByUserIdOrderByCreatedAtDescTest {

        @Test
        @DisplayName("특정 유저의 문서만 조회 및 생성일 기준 내림차순 정렬")
        void findByUserIdOrderByCreatedAtDesc_Success() {
            // given
            Long userId = 100L;
            LocalDateTime baseTime = LocalDateTime.now();

            // 첫 번째 문서 (가장 오래된)
            Document document1 = Document.builder()
                    .userId(userId)
                    .type("resume")
                    .filename("첫번째_이력서.pdf")
                    .content("첫 번째 이력서")
                    .link("https://s3.amazonaws.com/file1.pdf")
                    .build();
            document1.setCreatedAt(baseTime.minusDays(2)); // 2일 전
            entityManager.persist(document1);

            // 시간차를 두고 flush
            entityManager.flush();
            entityManager.clear();

            // 두 번째 문서 (중간)
            Document document2 = Document.builder()
                    .userId(userId)
                    .type("resume")
                    .filename("두번째_이력서.pdf")
                    .content("두 번째 이력서")
                    .link("https://s3.amazonaws.com/file2.pdf")
                    .build();
            document2.setCreatedAt(baseTime.minusDays(1)); // 1일 전
            entityManager.persist(document2);

            entityManager.flush();
            entityManager.clear();

            // 세 번째 문서 (가장 최근)
            Document document3 = Document.builder()
                    .userId(userId)
                    .type("portfolio")
                    .filename("포트폴리오.pdf")
                    .content("포트폴리오")
                    .link("https://s3.amazonaws.com/file3.pdf")
                    .build();
            document3.setCreatedAt(baseTime); // 현재
            entityManager.persist(document3);

            // 다른 유저의 문서 (결과에 포함되면 안됨)
            Document otherUserDocument = Document.builder()
                    .userId(200L)
                    .type("resume")
                    .filename("다른유저_이력서.pdf")
                    .content("다른 유저 이력서")
                    .link("https://s3.amazonaws.com/other.pdf")
                    .build();
            entityManager.persistAndFlush(otherUserDocument);

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Document> result = documentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);

            // 생성일 기준 내림차순 정렬 확인
            assertThat(result.getContent().get(0).getFilename()).isEqualTo("포트폴리오.pdf"); // 가장 최근
            assertThat(result.getContent().get(1).getFilename()).isEqualTo("두번째_이력서.pdf"); // 중간
            assertThat(result.getContent().get(2).getFilename()).isEqualTo("첫번째_이력서.pdf"); // 가장 오래된

            // 모든 문서가 해당 유저의 것인지 확인
            result.getContent().forEach(doc ->
                    assertThat(doc.getUserId()).isEqualTo(userId)
            );
        }

        @Test
        @DisplayName("문서가 없는 유저 조회 - 빈 페이지")
        void findByUserIdOrderByCreatedAtDesc_NoDocuments_EmptyPage() {
            // given
            Long userIdWithNoDocuments = 999L;
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Document> result = documentRepository.findByUserIdOrderByCreatedAtDesc(userIdWithNoDocuments, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0); // 빈 페이지의 경우 0이어야 함
        }

        @Test
        @DisplayName("페이지네이션 동작 확인")
        void findByUserIdOrderByCreatedAtDesc_Pagination_Success() {
            // given
            Long userId = 100L;
            LocalDateTime baseTime = LocalDateTime.now();

            // 5개의 문서 생성 (시간 순서를 명확하게)
            for (int i = 1; i <= 5; i++) {
                Document document = Document.builder()
                        .userId(userId)
                        .type("resume")
                        .filename("이력서_" + i + ".pdf")
                        .content("이력서 내용 " + i)
                        .link("https://s3.amazonaws.com/file" + i + ".pdf")
                        .build();
                document.setCreatedAt(baseTime.minusDays(5 - i)); // i가 클수록 최근
                entityManager.persist(document);
                entityManager.flush(); // 각각 즉시 flush하여 순서 보장
            }

            // 페이지 크기 2로 설정
            Pageable firstPage = PageRequest.of(0, 2);
            Pageable secondPage = PageRequest.of(1, 2);

            // when
            Page<Document> firstPageResult = documentRepository.findByUserIdOrderByCreatedAtDesc(userId, firstPage);
            Page<Document> secondPageResult = documentRepository.findByUserIdOrderByCreatedAtDesc(userId, secondPage);

            // then
            // 첫 번째 페이지
            assertThat(firstPageResult.getContent()).hasSize(2);
            assertThat(firstPageResult.getTotalElements()).isEqualTo(5);
            assertThat(firstPageResult.getTotalPages()).isEqualTo(3);
            assertThat(firstPageResult.isFirst()).isTrue();
            assertThat(firstPageResult.hasNext()).isTrue();

            // 두 번째 페이지
            assertThat(secondPageResult.getContent()).hasSize(2);
            assertThat(secondPageResult.getTotalElements()).isEqualTo(5);
            assertThat(secondPageResult.getTotalPages()).isEqualTo(3);
            assertThat(secondPageResult.isFirst()).isFalse();
            assertThat(secondPageResult.hasNext()).isTrue();
        }

        @Test
        @DisplayName("삭제된 문서도 포함하여 조회 (soft delete 미적용)")
        void findByUserIdOrderByCreatedAtDesc_IncludeDeletedDocuments() {
            // given
            Long userId = 100L;

            // 정상 문서
            Document normalDocument = Document.builder()
                    .userId(userId)
                    .type("resume")
                    .filename("정상_이력서.pdf")
                    .content("정상 이력서")
                    .link("https://s3.amazonaws.com/normal.pdf")
                    .deletedAt(null)
                    .build();
            entityManager.persist(normalDocument);

            // 삭제된 문서
            Document deletedDocument = Document.builder()
                    .userId(userId)
                    .type("portfolio")
                    .filename("삭제된_포트폴리오.pdf")
                    .content("삭제된 포트폴리오")
                    .link("https://s3.amazonaws.com/deleted.pdf")
                    .deletedAt(LocalDateTime.now())
                    .build();
            entityManager.persistAndFlush(deletedDocument);

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Document> result = documentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

            // then
            // 이 메소드는 deletedAt을 고려하지 않으므로 삭제된 문서도 포함
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(Document::getFilename)
                    .containsExactlyInAnyOrder("정상_이력서.pdf", "삭제된_포트폴리오.pdf");
        }
    }

    @Nested
    @DisplayName("기본 CRUD 동작 테스트")
    class BasicCrudTest {

        @Test
        @DisplayName("문서 저장 및 조회")
        void saveAndFind_Success() {
            // given
            Document document = Document.builder()
                    .userId(100L)
                    .type("resume")
                    .filename("테스트_이력서.pdf")
                    .content("테스트 이력서 내용")
                    .link("https://s3.amazonaws.com/test.pdf")
                    .build();

            // when
            Document savedDocument = documentRepository.save(document);
            Optional<Document> foundDocument = documentRepository.findById(savedDocument.getId());

            // then
            assertThat(foundDocument).isPresent();
            assertThat(foundDocument.get().getUserId()).isEqualTo(100L);
            assertThat(foundDocument.get().getType()).isEqualTo("resume");
            assertThat(foundDocument.get().getFilename()).isEqualTo("테스트_이력서.pdf");
            assertThat(foundDocument.get().getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("문서 수정")
        void update_Success() {
            // given
            Document document = Document.builder()
                    .userId(100L)
                    .type("resume")
                    .filename("원본_이력서.pdf")
                    .content("원본 내용")
                    .link("https://s3.amazonaws.com/original.pdf")
                    .build();

            Document savedDocument = entityManager.persistAndFlush(document);

            // Entity를 clear하여 영속성 컨텍스트에서 분리
            entityManager.clear();

            // when
            // 다시 조회하여 수정
            Optional<Document> foundDocument = documentRepository.findById(savedDocument.getId());
            assertThat(foundDocument).isPresent();

            Document documentToUpdate = foundDocument.get();
            documentToUpdate.setContent("수정된 내용");
            documentToUpdate.setUpdatedAt(LocalDateTime.now()); // updatedAt 명시적 설정

            Document updatedDocument = documentRepository.save(documentToUpdate);

            // then
            assertThat(updatedDocument.getContent()).isEqualTo("수정된 내용");
            assertThat(updatedDocument.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("문서 삭제 (soft delete)")
        void softDelete_Success() {
            // given
            Document document = Document.builder()
                    .userId(100L)
                    .type("resume")
                    .filename("삭제할_이력서.pdf")
                    .content("삭제할 내용")
                    .link("https://s3.amazonaws.com/delete.pdf")
                    .build();

            Document savedDocument = entityManager.persistAndFlush(document);

            // when
            savedDocument.setDeletedAt(LocalDateTime.now());
            documentRepository.save(savedDocument);

            // then
            Optional<Document> deletedDocument = documentRepository.findById(savedDocument.getId());
            assertThat(deletedDocument).isPresent();
            assertThat(deletedDocument.get().getDeletedAt()).isNotNull();

            // findByIdAndDeletedAtIsNull로는 조회되지 않음
            Optional<Document> notFoundDeleted = documentRepository.findByIdAndDeletedAtIsNull(savedDocument.getId());
            assertThat(notFoundDeleted).isEmpty();
        }
    }
}
