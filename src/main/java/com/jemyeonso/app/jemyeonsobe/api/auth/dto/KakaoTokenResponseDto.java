package com.jemyeonso.app.jemyeonsobe.api.auth.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoTokenResponseDto {
    private String access_token;
    private String refresh_token;
}