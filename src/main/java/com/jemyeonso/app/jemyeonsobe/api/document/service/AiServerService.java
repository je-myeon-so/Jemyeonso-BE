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

            log.info("=== AI Server Request Start ===");
            log.info("Document ID: {}", documentId);
            log.info("User ID: {}", userId);
            log.info("File Type: {}", fileType);
            log.info("File URL: {}", fileUrl);
            log.info("Request Data: {}", request);
            log.info("Sending to AI server...");

            // AI 서버에 비동기 요청 전송
            aiWebClient.post()
                    .uri("/api/ai/file")  // 수정된 AI 서버의 엔드포인트
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> {
                        log.info("=== AI Server Response Success ===");
                        log.info("Document ID: {}", documentId);
                        log.info("Response: {}", response);
                        log.info("=== AI Server Request Complete ===");
                    })
                    .doOnError(error -> {
                        log.error("=== AI Server Request Failed ===");
                        log.error("Document ID: {}", documentId);
                        log.error("Error Type: {}", error.getClass().getSimpleName());
                        log.error("Error Message: {}", error.getMessage());
                        log.error("Full Error: ", error);
                        log.error("=== AI Server Request Failed End ===");
                    })
                    .subscribe();

        } catch (Exception e) {
            log.error("=== Failed to send document to AI server ===");
            log.error("Document ID: {}", documentId);
            log.error("Exception: ", e);
        }
    }
}
