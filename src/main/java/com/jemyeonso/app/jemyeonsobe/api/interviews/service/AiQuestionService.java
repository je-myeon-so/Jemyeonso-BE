package com.jemyeonso.app.jemyeonsobe.api.interviews.service;

import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Question;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiQuestionService {

    private final QuestionRepository questionRepository;

    @Qualifier("aiWebClient")
    private final WebClient aiWebClient;

    public Question requestAndSaveQuestion(Long interviewId, AiQuestionRequest request) {
        // AI 서버로 POST 요청
        AiQuestionResponse aiResponse = aiWebClient.post()
                .uri("/api/questions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiQuestionResponse.class)
                .block();

        if (aiResponse == null || aiResponse.getData() == null || aiResponse.getData().getQuestion() == null) {
            throw new RuntimeException("AI 서버에서 질문을 받아오지 못했습니다.");
        }

        String questionContent = aiResponse.getData().getQuestion();

        // Question 엔티티 생성
        Question question = Question.builder()
                .interviewId(interviewId)
                .content(questionContent)
                .createdAt(LocalDateTime.now())
                .build();

        // DB에 저장
        return questionRepository.save(question);

//        // 더미 응답
//        String questionContent = "이것은 더미 테스트 질문입니다. (레벨: "
//                + request.getQuestionLevel() + ", 직무: "
//                + request.getJobType() + ", 유형: "
//                + request.getQuestionCategory() + ")";
//
//        // Question 엔티티 생성
//        Question question = Question.builder()
//                .interviewId(interviewId)
//                .content(questionContent)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        // DB에 저장
//        return questionRepository.save(question);
    }
}
