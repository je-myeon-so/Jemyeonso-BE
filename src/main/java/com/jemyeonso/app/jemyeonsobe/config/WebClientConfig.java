package com.jemyeonso.app.jemyeonsobe.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${KAKAO_ADMIN_KEY}")
    private String adminKey;

    @Value("${AI_API_BASE_URL}")
    private String aiApiBaseUrl;

    @Value("${KAKAO_AUTH_BASE_URL}")
    private String kakaoAuthBaseUrl;

    @Value("${KAKAO_API_BASE_URL}")
    private String kakaoApiBaseUrl;


    // OAuth 인증용 (토큰 발급용)
    @Bean
    public WebClient kakaoAuthClient() {
        return WebClient.builder()
            .baseUrl(kakaoAuthBaseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();
    }

    // 사용자 API 호출용
    @Bean
    public WebClient kakaoApiClient() {
        return WebClient.builder()
            .baseUrl(kakaoApiBaseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey)
            .build();
    }

    @Bean
    @Qualifier("aiWebClient")
    public WebClient aiWebClient() {
        return WebClient.builder()
                .baseUrl(aiApiBaseUrl)
                .build();
    }
}