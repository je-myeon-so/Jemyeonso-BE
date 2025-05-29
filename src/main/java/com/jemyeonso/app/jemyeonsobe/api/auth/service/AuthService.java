package com.jemyeonso.app.jemyeonsobe.api.auth.service;

import com.jemyeonso.app.jemyeonsobe.api.auth.dto.KakaoUserResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.auth.entity.Oauth;
import com.jemyeonso.app.jemyeonsobe.api.auth.repository.AuthRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserRepository;
import com.jemyeonso.app.jemyeonsobe.common.exception.UnauthorizedException;
import com.jemyeonso.app.jemyeonsobe.util.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final KakaoOauthClient kakaoOauthClient;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public void loginWithKakao(String code, HttpServletResponse response) {
        KakaoUserResponseDto kakaouser;

        // Kakao 유저 정보 가져오기
        try {
            kakaouser = kakaoOauthClient.getUserInfo(code);
        } catch (Exception e) {
            log.error("카카오 유저 정보 가져오기 실패", e);
            throw new RuntimeException("카카오 API 호출 실패", e);
        }

        String kakaoId = kakaouser.getProviderId();
        String name = kakaouser.getName();
        String email = kakaouser.getEmail();
        String profileImageUrl = kakaouser.getProfileImageUrl();

        Optional<Oauth> authOpt;

        // 기존 사용자 조회
        try {
            authOpt = authRepository.findByProviderAndProviderId("KAKAO", kakaoId);
        } catch (Exception e) {
            log.error("DB에서 Oauth 정보 조회 실패", e);
            throw new RuntimeException("DB 조회 실패", e);
        }

        // 기존 사용자 처리
        if (authOpt.isPresent()) {
            User existingUser = authOpt.get().getUser();

            try {
                String refreshToken = jwtTokenProvider.createRefreshToken(existingUser);
                String accessToken = jwtTokenProvider.createAccessToken(existingUser);

                authRepository.updateRefreshToken(existingUser.getId(), refreshToken);

                addTokenCookies(response, accessToken, refreshToken);
            } catch (Exception e) {
                log.error("기존 사용자 토큰 발급 실패", e);
                throw new RuntimeException("토큰 발급 실패", e);
            }

        } else {
            // 신규 사용자 처리
            try {
                User newUser = User.builder()
                    .nickname(name)
                    .name(name)
                    .email(email)
                    .profileImgUrl(profileImageUrl)
                    .createdAt(LocalDateTime.now())
                    .build();
                userRepository.save(newUser);

                String refreshToken = jwtTokenProvider.createRefreshToken(newUser);
                String accessToken = jwtTokenProvider.createAccessToken(newUser);

                Oauth newOauth = Oauth.builder()
                    .provider("KAKAO")
                    .providerId(kakaoId)
                    .user(newUser)
                    .refreshToken(refreshToken)
                    .build();
                authRepository.save(newOauth);

                addTokenCookies(response, accessToken, refreshToken);

            } catch (Exception e) {
                throw new RuntimeException("신규 사용자 처리 실패", e);
            }
        }
    }

    /**
     * 로그아웃
     * @param request
     * @param response
     * @return
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = extractTokenFromCookies(request);

        try {
            if (accessToken == null) {
                log.info(">>>>> (LogoutService) Access token is null");
                throw new UnauthorizedException("유효하지 않은 access 토큰입니다.");
            }

            if (!jwtTokenProvider.isInvalidToken(accessToken)) {
                throw new UnauthorizedException("잘못된 access 토큰입니다.");
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);

            // DB에서 RefreshToken null 처리
            authRepository.findByUserId(userId).ifPresent(oAuth -> {
                oAuth.setRefreshToken(null);
                authRepository.save(oAuth);
            });

            // 쿠키 제거
            invalidateCookie(response, "access_token");
            invalidateCookie(response, "refresh_token");

        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("만료된 access 토큰입니다.");
        }
    }

    /**
     * 리프레시 토큰을 기반으로 한 토큰 재발급
     * @param request
     * @param response
     */
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractTokenFromCookies(request);

        if (!jwtTokenProvider.isInvalidToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        Optional<Oauth> oauthOpt = Optional.ofNullable(authRepository.findByUserId(userId)
            .orElseThrow(() -> new UnauthorizedException("OAuth 정보가 존재하지 않습니다.")));

        User user = oauthOpt.get().getUser();
        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);

        // DB에 새 refreshToken 저장
        authRepository.updateRefreshToken(user.getId(), newRefreshToken);

        // accessToken 쿠키로 전달
        Cookie accessCookie = new Cookie("access_token", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(30 * 60);
        response.addCookie(accessCookie);

        // RefreshToken 쿠키로 전달
        Cookie refreshCookie = new Cookie("refresh_token", newRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);
    }

    private void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setMaxAge(30 * 60);      // 30분
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);  // 7일
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);
    }

    /**
     * 쿠키에서 토큰 추출
     * @param request
     * @return
     */
    private String extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("access_token".equals(cookie.getName())) {
                return cookie.getValue();
            } else if ("refresh_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void invalidateCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
    }
}