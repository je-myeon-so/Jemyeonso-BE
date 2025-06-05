package com.jemyeonso.app.jemyeonsobe.api.interviews.repository;

import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Question;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByInterviewId(Long interviewId);
}
