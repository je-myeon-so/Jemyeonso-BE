package com.jemyeonso.app.jemyeonsobe.api.auth.service;

import com.jemyeonso.app.jemyeonsobe.api.auth.dto.KakaoUserResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.auth.entity.Oauth;
import com.jemyeonso.app.jemyeonsobe.api.auth.repository.AuthRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserRepository;
import com.jemyeonso.app.jemyeonsobe.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
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
            // 4️⃣ 신규 사용자 처리
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
}