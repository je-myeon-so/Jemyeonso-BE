package com.jemyeonso.app.jemyeonsobe.api.document.repository;

import com.jemyeonso.app.jemyeonsobe.api.document.entity.Document;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByIdAndDeletedAtIsNull(Long documentId);

    // 사용자 Id로 작성된 문서 삭제
    @Modifying
    @Query("DELETE FROM Document d WHERE d.userId = :userId")
    void deleteByUserId(@Param("userId")Long userId);

    // 특정 유저의 문서만 조회 (생성일 기준 내림차순)
    @Query("SELECT d FROM Document d WHERE d.userId = :userId ORDER BY d.createdAt DESC")
    Page<Document> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Document d
           SET d.deletedAt = :now,
               d.updatedAt = :now
         WHERE d.userId = :userId
           AND d.deletedAt IS NULL
    """)
    int softDeleteByUserId(@Param("userId") Long userId,
        @Param("now") LocalDateTime now);

    List<Document> findAllByUserIdAndDeletedAtIsNull(Long userId);
}
