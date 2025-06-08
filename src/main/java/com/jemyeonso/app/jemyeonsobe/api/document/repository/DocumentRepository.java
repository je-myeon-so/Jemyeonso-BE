package com.jemyeonso.app.jemyeonsobe.api.document.repository;

import com.jemyeonso.app.jemyeonsobe.api.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // 특정 유저의 문서만 조회 (생성일 기준 내림차순)
    @Query("SELECT d FROM Document d WHERE d.userId = :userId ORDER BY d.createdAt DESC")
    Page<Document> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
}
