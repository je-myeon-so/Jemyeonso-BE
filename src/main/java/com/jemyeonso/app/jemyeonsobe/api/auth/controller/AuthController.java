package com.jemyeonso.app.jemyeonsobe.api.auth.controller;

import com.jemyeonso.app.jemyeonsobe.api.auth.dto.AuthResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.auth.service.AuthService;
import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponseCode;
import com.jemyeonso.app.jemyeonsobe.common.exception.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backend/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 카카오 콜백 인가 API
     * @param code
     * @param response
     * @return
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code,
        HttpServletResponse response) {
        System.out.println("kakao callback");
        authService.loginWithKakao(code, response);
        ResponseEntity<ApiResponse<Object>> ok = ResponseEntity.ok(
            ApiResponse.success(ApiResponseCode.SUCCESS, "카카오 콜백에 성공하였습니다.", null));
        return ok;
    }
}
