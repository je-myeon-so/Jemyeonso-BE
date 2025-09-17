package com.jemyeonso.app.jemyeonsobe.api.user.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jemyeonso.app.jemyeonsobe.api.user.service.ai.dto.ImproveRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.user.service.ai.dto.ImproveRequestDto.QaItem;
import com.jemyeonso.app.jemyeonsobe.api.user.service.ai.dto.ImproveResponseDto;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiImproveService {

    @Qualifier("aiWebClient")
    private final WebClient aiWebClient;

    private final ObjectMapper objectMapper;

    @Value("${ai.mock.enabled:false}")
    boolean mockEnabled;

    public String fetchOverallComment(Long interviewId, String jobType, List<QaItem> qaList) {
        ImproveRequestDto req = ImproveRequestDto.builder()
            .interviewId(interviewId)
            .jobType(jobType)
            .qaList(qaList)         // documentID말고 질문-답변 리스트 AI한테 전송
            .build();

        if (mockEnabled) {
            log.warn("local환경: 목 응답으로 대체 (qaList size={})",
                qaList == null ? 0 : qaList.size());

            ExchangeFunction mockEx = clientRequest -> {
                Map<String, Object> body = Map.of(
                    "code", 200,
                    "message", "면접 세션 종합 분석이 완료되었습니다.(MOCK)",
                    "data", Map.of(
                        "interviewId", interviewId,
                        "overallComment", "조금 더 당당하게 말하세요"
                    )
                );
                String json;
                try {
                    json = objectMapper.writeValueAsString(body);
                } catch (JsonProcessingException e) {
                    json = "{\"code\":200,\"data\":{\"overallComment\":\"MOCK\"}}";
                }

                ClientResponse resp = ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(json)
                    .build();

                return Mono.just(resp);
            };

            WebClient mockClient = WebClient.builder()
                .exchangeFunction(mockEx)
                .build();

            ImproveResponseDto res = mockClient.post()
                .uri("/api/ai/improve")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(ImproveResponseDto.class)
                .block();

            if (res == null || res.getData() == null || res.getData().getOverallComment() == null) {
                throw new IllegalStateException("AI MOCK 응답이 비어있습니다.");
            }
            return res.getData().getOverallComment();
        }

        // 실서버 호출
        ImproveResponseDto res = aiWebClient.post()
            .uri("/api/ai/improve")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .retrieve()
            .bodyToMono(ImproveResponseDto.class)
            .block();

        if (res == null || res.getData() == null || res.getData().getOverallComment() == null) {
            throw new IllegalStateException("AI 서버 개선점 응답이 비어있습니다.");
        }
        return res.getData().getOverallComment();
    }
}
