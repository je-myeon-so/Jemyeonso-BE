package com.jemyeonso.app.jemyeonsobe.api.interviews.repository;

import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Query("SELECT i FROM Interview i JOIN FETCH i.document ORDER BY i.createdAt DESC")
    Page<Interview> findAllWithDocument(Pageable pageable);

    // 특정 유저의 면접만 조회 (Document와 함께)
    @Query("SELECT i FROM Interview i JOIN FETCH i.document WHERE i.userId = :userId ORDER BY i.createdAt DESC")
    Page<Interview> findByUserIdWithDocument(@Param("userId") Long userId, Pageable pageable);
}
