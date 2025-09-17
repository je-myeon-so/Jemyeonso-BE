package com.jemyeonso.app.jemyeonsobe.api.interviews.repository;

import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Question;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.projection.QaRow;
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


    // 특정 인터뷰에 속한 질문, 답변을 쌍으로 가져옴 (QaRow에 매핑됨)
    @Query(value = """
      SELECT q.id AS questionId,
             q.content AS question,
             (
               SELECT a2.content
               FROM answer a2
               WHERE a2.question_id = q.id
               ORDER BY a2.id DESC
               LIMIT 1
             ) AS answer
      FROM questions q
      WHERE q.interview_id = :interviewId
      ORDER BY q.created_at ASC
      """, nativeQuery = true)
    List<QaRow> findQaRowsByInterviewId(Long interviewId);
}


