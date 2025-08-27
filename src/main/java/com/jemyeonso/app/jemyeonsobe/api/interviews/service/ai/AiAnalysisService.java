package com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai;

import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Answer;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Feedback;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Interview;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.FeedbackRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.dto.AiAnswerAnalyzeRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.dto.AiAnswerAnalyzeResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.dto.AiAnswerAnalyzeResponseDto.Analysis;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    @Qualifier("aiWebClient")
    private final WebClient aiWebClient;

    private final FeedbackRepository feedbackRepository;

    @Async
    public void analyzeAnswerAsync(Answer answer, Interview interview, String previousQuestion) {
        AiAnswerAnalyzeRequestDto request = AiAnswerAnalyzeRequestDto.builder()
            .questionLevel(interview.getQuestionLevel().name())
            .jobType(interview.getJobtype())
            .questionCategory(interview.getQuestionCategory().name())
            .question(previousQuestion)
            .answer(answer.getContent())
            .build();

        aiWebClient.post()
            .uri("/api/ai/answers/analyze")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(AiAnswerAnalyzeResponseDto.class)
            .doOnNext(response -> {
                List<Analysis> analysisList = response.getData().getAnalysis();
                for (AiAnswerAnalyzeResponseDto.Analysis analysis : analysisList) {
                    Feedback feedback = Feedback.builder()
                        .answerId(answer.getId())
                        .errorText(analysis.getErrorText())
                        .errorType(analysis.getErrorType())
                        .feedback(analysis.getFeedback())
                        .suggestion(analysis.getSuggestion())
                        .build();
                    feedbackRepository.save(feedback);
                }
            })
            .onErrorResume(e -> {
                // 에러 로그
                log.error("답변 분석 중 오류 발생: {}", e.getMessage());
                return Mono.empty();
            })
            .subscribe();
    }
}

//        // Mock 응답 ---
//        List<AiAnswerAnalyzeResponse.Analysis> mockAnalysisList = List.of(
//            new AiAnswerAnalyzeResponse.Analysis("발음이 부정확합니다", "발음 실수", "발음이 불명확하여 전달력이 떨어집니다", "또박또박 말하는 연습이 필요합니다"),
//            new AiAnswerAnalyzeResponse.Analysis("전문 용어 사용이 부족합니다", "전문성 부족", "답변에 해당 직무와 관련된 용어가 부족합니다", "관련 용어를 더 포함해보세요")
//        );
//
//        for (AiAnswerAnalyzeResponse.Analysis analysis : mockAnalysisList) {
//            Feedback feedback = Feedback.builder()
//                .answerId(answer.getId())
//                .errorText(analysis.getErrorText())
//                .errorType(analysis.getErrorType())
//                .feedback(analysis.getFeedback())
//                .suggestion(analysis.getSuggestion())
//                .build();
//            feedbackRepository.save(feedback);
//        }
//
//        System.out.println("비동기 목업 분석 완료: answerId = " + answer.getId());
//    }
//}
//
