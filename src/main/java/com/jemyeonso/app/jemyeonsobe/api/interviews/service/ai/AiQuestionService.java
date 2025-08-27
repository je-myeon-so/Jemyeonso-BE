package com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai;

import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Question;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.QuestionRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.dto.AiQuestionRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.dto.AiQuestionResponseDto;
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

    public AiQuestionResponseDto requestAndSaveQuestion(Long interviewId,
        AiQuestionRequestDto request) {
        AiQuestionResponseDto aiResponse = aiWebClient.post()
            .uri("/api/ai/questions")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(AiQuestionResponseDto.class)
            .block();

        if (aiResponse == null
            || aiResponse.getData() == null
            || aiResponse.getData().getQuestion() == null) {
            throw new RuntimeException("AI 서버에서 질문을 받아오지 못했습니다.");
        }

        // 질문 저장
        Question question = Question.builder()
                .interviewId(interviewId)
                .content(aiResponse.getData().getQuestion())
                .questionType(aiResponse.getData().getQuestionType())
                .createdAt(LocalDateTime.now())
                .build();
        questionRepository.save(question);

        AiQuestionResponseDto.AiQuestionData updatedData = aiResponse.getData();
        updatedData.setQuestionId(question.getId());

        aiResponse.setData(updatedData); // 필요 시 생략 가능

        // 그대로 리턴
        return aiResponse;
    }
}
    //            // 실제 WebClient 호출 대신 더미 응답 생성
//            String dummyQuestion = "이것은 후속 질문 테스트입니다. (이전 질문: " + request.getPreviousQuestion() + ")";
//            String dummyQuestionType = "TECHNOLOGY"; // 또는 PERSONAL, BEHAVIORAL 등
//
//            // 질문 저장
//            Question question = Question.builder()
//                .interviewId(interviewId)
//                .content(dummyQuestion)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//            questionRepository.save(question);
//
//            // 더미 AI 응답 객체 리턴
//            return new AiQuestionResponseDto(
//                200,
//                "질문을 생성했습니다.",
//                new AiQuestionResponseDto.AiQuestionData(dummyQuestion, dummyQuestionType)
//            );
//        }
//
//    }
