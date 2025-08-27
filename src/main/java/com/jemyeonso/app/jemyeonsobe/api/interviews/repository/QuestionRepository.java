package com.jemyeonso.app.jemyeonsobe.api.interviews.repository;

import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByInterviewIdOrderByCreatedAtAsc(Long interviewId);

    // 특정 질문 조회 (면접 ID로 권한 검증용)
    @Query("SELECT q FROM Question q WHERE q.id = :questionId AND q.interviewId = :interviewId")
    Optional<Question> findByIdAndInterviewId(@Param("questionId") Long questionId, @Param("interviewId") Long interviewId);

}


