package com.jemyeonso.app.jemyeonsobe.api.auth.controller;

import com.jemyeonso.app.jemyeonsobe.api.auth.service.AuthService;
import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponseCode;
import com.jemyeonso.app.jemyeonsobe.common.exception.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backend/auth")
@Tag(name = "Auth", description = "카카오 (Oauth) 관련 API입니다.")
public class AuthController {

    private final AuthService authService;

    /**
     * 카카오 콜백 인가 API
     * @param code
     * @param response
     * @return
     */
    @GetMapping("/kakao/callback")
    @Operation(summary = "카카오 콜백 인가", description = "프론트에서 전달한 카카오 인가 코드를 통해 사용자 로그인 및 회원가입을 처리합니다.")
    public ResponseEntity<?> kakaoCallback(
        @Parameter(description = "카카오 인가 코드", example = "tfIwXlgph5T8g-tRuEdHdhmzT2m2aZRXqXuYpaVZUO")
        @RequestParam("code") String code,
        HttpServletResponse response) {
        authService.loginWithKakao(code, response);
        return ResponseEntity.ok(
            ApiResponse.success(ApiResponseCode.SUCCESS, "카카오 콜백에 성공하였습니다.", null));
    }

    /**
     * 로그아웃 API
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자의 access token과 refresh token을 만료 처리합니다.")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Access Token 재발급", description = "refresh token을 이용해 새로운 access token을 발급받습니다.")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        authService.refreshAccessToken(request, response);
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.TOKEN_REFRESH_SUCCESS, "토큰이 성공적으로 재발급되었습니다.", null));
    }
}
