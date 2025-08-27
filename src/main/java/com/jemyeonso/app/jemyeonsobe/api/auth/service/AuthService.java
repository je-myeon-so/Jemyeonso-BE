package com.jemyeonso.app.jemyeonsobe.api.auth.service;

import com.jemyeonso.app.jemyeonsobe.api.auth.dto.KakaoUserResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.auth.entity.Oauth;
import com.jemyeonso.app.jemyeonsobe.api.auth.repository.AuthRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserRepository;
import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;
import com.jemyeonso.app.jemyeonsobe.common.exception.UnauthorizedException;
import com.jemyeonso.app.jemyeonsobe.util.CookieUtil;
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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final KakaoOauthClient kakaoOauthClient;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;

    @Transactional
    public void loginWithKakao(String code, HttpServletResponse response) {
        KakaoUserResponseDto kakaouser;
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

        // providerId로 Oauth 링크 먼저 탐색
        Optional<Oauth> oauthOpt = authRepository.findByProviderAndProviderId("KAKAO", kakaoId);

        if (oauthOpt.isPresent()) {
            // Oauth가 이미 존재 → 연결된 유저 확인
            User user = oauthOpt.get().getUser();

            // 탈퇴 상태면 복구 (재가입)
            if (user.getDeletedAt() != null) {
                user.setDeletedAt(null);
                user.setUpdatedAt(LocalDateTime.now());
                // 필요 시 프로필 동기화
                user.setNickname(name);
                user.setName(name);
                user.setEmail(email);
                user.setProfileImgUrl(profileImageUrl);
                // userRepository.save(user);
            }

            issueAndAttachTokens(response, user);
            return;
        }

        // Oauth가 없으면, 이메일 기준으로 '탈퇴 포함' 사용자 찾기
        Optional<User> userByEmailAny = userRepository.findByEmailIncludingDeleted(email);
        if (userByEmailAny.isPresent()) {
            User user = userByEmailAny.get();

            if (user.getDeletedAt() != null) {
                // 탈퇴 유저 복구 후 새 Oauth row 생성
                user.setDeletedAt(null);
                user.setUpdatedAt(LocalDateTime.now());
                user.setNickname(name);
                user.setName(name);
                user.setProfileImgUrl(profileImageUrl);
                // userRepository.save(user);

                Oauth newOauth = Oauth.builder()
                    .provider("KAKAO")
                    .providerId(kakaoId)
                    .user(user)
                    .refreshToken(null)
                    .build();
                authRepository.save(newOauth);

                issueAndAttachTokens(response, user);
                return;
            } else {
                // OAuth 만 신규 연결
                Oauth newOauth = Oauth.builder()
                    .provider("KAKAO")
                    .providerId(kakaoId)
                    .user(userByEmailAny.get())
                    .refreshToken(null)
                    .build();
                authRepository.save(newOauth);

                issueAndAttachTokens(response, userByEmailAny.get());
                return;
            }
        }

        // 3) 신규 가입
        User newUser = User.builder()
            .nickname(name)
            .name(name)
            .email(email)
            .profileImgUrl(profileImageUrl)
            .createdAt(LocalDateTime.now())
            .build();
        userRepository.save(newUser);

        Oauth newOauth = Oauth.builder()
            .provider("KAKAO")
            .providerId(kakaoId)
            .user(newUser)
            .refreshToken(null)
            .build();
        authRepository.save(newOauth);

        issueAndAttachTokens(response, newUser);
    }

    private void issueAndAttachTokens(HttpServletResponse response, User user) {
        try {
            String refreshToken = jwtTokenProvider.createRefreshToken(user);
            String accessToken = jwtTokenProvider.createAccessToken(user);

            authRepository.updateRefreshToken(user.getId(), refreshToken); // 저장 정책에 맞게
            addTokenCookies(response, accessToken, refreshToken);
        } catch (Exception e) {
            log.error("토큰 발급/저장 실패", e);
            throw new RuntimeException("토큰 발급 실패", e);
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
                throw new UnauthorizedException(ErrorMessage.INVALID_ACCESS_TOKEN);
            }

            if (!jwtTokenProvider.isValidToken(accessToken)) {
                throw new UnauthorizedException(ErrorMessage.INVALID_ACCESS_TOKEN);
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);

            // DB에서 RefreshToken null 처리
            authRepository.findByUserId(userId).ifPresent(oAuth -> {
                oAuth.setRefreshToken(null);
                authRepository.save(oAuth);
            });

            // 쿠키 제거
            cookieUtil.invalidateCookie(response, "access_token");
            cookieUtil.invalidateCookie(response, "refresh_token");

        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorMessage.ACCESS_TOKEN_EXPIRED);
        }
    }

    /**
     * 리프레시 토큰을 기반으로 한 토큰 재발급
     * @param request
     * @param response
     */
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractTokenFromCookies(request);

        if (!jwtTokenProvider.isValidToken(refreshToken)) {
            throw new UnauthorizedException(ErrorMessage.INVALID_ACCESS_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        System.out.println("userId: " + userId);

        Optional<Oauth> oauthOpt = Optional.ofNullable(authRepository.findByUserId(userId)
            .orElseThrow(() -> new UnauthorizedException(ErrorMessage.OAUTH_NOT_FOUND)));

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
}