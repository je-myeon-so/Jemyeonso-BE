package com.jemyeonso.app.jemyeonsobe.api.interviews.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class AiClient {

    private final WebClient webClient;

    public AiClient(@Qualifier("aiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public String generateQuestion(String questionLevel, String jobType, String questionType) {
//        AiQuestionRequest request = new AiQuestionRequest(questionLevel, jobType, questionType);
//
//        AiQuestionResponse response = webClient.post()
//                .uri("/api/ai/questions")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .retrieve()
//                .onStatus(status -> status.isError(), clientResponse -> {
//                    throw new RuntimeException("AI 서버에서 질문 생성 실패");
//                })
//                .bodyToMono(AiQuestionResponse.class)
//                .block();
//
//        if (response == null || response.getData() == null) {
//            log.error("AI 서버 응답이 null입니다.");
//            throw new RuntimeException("AI 서버 응답이 null입니다.");
//        }

        // 임시 더미 응답
        return "이것은 AI 서버가 생성한 테스트 질문입니다.";
//        return response.getData().getQuestion();
    }
}
