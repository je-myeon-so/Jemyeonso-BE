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

    // OAuth 인증용 (토큰 발급용)
    @Bean
    public WebClient kakaoAuthClient() {
        return WebClient.builder()
            .baseUrl("https://kauth.kakao.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();
    }

    // 사용자 API 호출용
    @Bean
    public WebClient kakaoApiClient() {
        return WebClient.builder()
            .baseUrl("https://kapi.kakao.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey)
            .build();
    }

    @Bean
    @Qualifier("aiWebClient")
    public WebClient aiWebClient() {
        return WebClient.builder()
                .baseUrl("http://172.20.5.180:8000")     // 도커 컨테이너 네트워크 이름
                .build();
    }
}
