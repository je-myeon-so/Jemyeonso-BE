package com.jemyeonso.app.jemyeonsobe.api.interviews.repository;

import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // 특정 질문의 답변 조회 (피드백과 함께)
    @Query("SELECT a FROM Answer a LEFT JOIN FETCH a.feedbacks WHERE a.questionId = :questionId")
    Optional<Answer> findByQuestionIdWithFeedbacks(@Param("questionId") Long questionId);
    
    // 평균 점수 조회 쿼리
    @Query("""
        select avg(a.score)
        from Answer a
        where a.questionId in (
            select q.id from Question q
            where q.interviewId = :interviewId
        )
        """)
    Double findAverageScoreByInterviewId(Long interviewId);
}
