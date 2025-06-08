package com.jemyeonso.app.jemyeonsobe.api.document.service;

import com.jemyeonso.app.jemyeonsobe.api.document.dto.AiServerRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServerService {

    @Qualifier("aiWebClient")
    private final WebClient aiWebClient;

    @Async
    public void sendDocumentToAiServer(String fileUrl, Long userId, Long documentId, String fileType) {
        try {
            AiServerRequestDto request = AiServerRequestDto.builder()
                    .fileUrl(fileUrl)
                    .userId(userId)
                    .documentId(documentId)
                    .fileType(fileType)
                    .build();

            // AI 서버에 비동기 요청 전송
            aiWebClient.post()
                    .uri("/api/documents/process")  // AI 서버의 엔드포인트
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> {
                        log.info("AI server request successful for document: {}", documentId);
                    })
                    .doOnError(error -> {
                        log.error("AI server request failed for document: {}", documentId, error);
                    })
                    .subscribe();

        } catch (Exception e) {
            log.error("Failed to send document to AI server", e);
        }
    }
}
