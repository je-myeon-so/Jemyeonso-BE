package com.jemyeonso.app.jemyeonsobe.api.user.service;

import com.jemyeonso.app.jemyeonsobe.api.auth.entity.Oauth;
import com.jemyeonso.app.jemyeonsobe.api.auth.repository.AuthRepository;
import com.jemyeonso.app.jemyeonsobe.api.auth.service.AuthService;
import com.jemyeonso.app.jemyeonsobe.api.auth.service.KakaoOauthClient;
import com.jemyeonso.app.jemyeonsobe.api.document.entity.Document;
import com.jemyeonso.app.jemyeonsobe.api.document.repository.DocumentRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserFeedbackResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserInfoResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserRepository;
import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;
import com.jemyeonso.app.jemyeonsobe.common.exception.ResourceNotFoundException;
import com.jemyeonso.app.jemyeonsobe.common.exception.UnauthorizedException;
import com.jemyeonso.app.jemyeonsobe.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final KakaoOauthClient kakaoOauthClient;
    private final CookieUtil cookieUtil;
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(()->new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

        return UserInfoResponseDto.builder()
            .userId(user.getId())
            .profileImgUrl(user.getProfileImgUrl())
            .name(user.getName())
            .nickname(user.getNickname())
            .email(user.getEmail())
            .comment(user.getComment())
            .build();
    }

    public UserInfoResponseDto patchUserInfo(Long userId, String nickname, String profileImgUrl, String comment) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(()->new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

        if (nickname != null) user.setNickname(nickname);
        if (profileImgUrl != null) user.setProfileImgUrl(profileImgUrl);
        if (comment != null) user.setComment(comment);

        return UserInfoResponseDto.builder()
            .userId(user.getId())
            .profileImgUrl(user.getProfileImgUrl())
            .name(user.getName())
            .nickname(user.getNickname())
            .email(user.getEmail())
            .comment(user.getComment())
            .build();
    }

    public UserFeedbackResponseDto getImprovement(Long userId) {
        // 유저 찾기
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(()->new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

        return UserFeedbackResponseDto.builder()
            .userId(user.getId())
            .feedback(user.getImprovement())
            .build();
    }

    @Transactional
    public void deleteUser(Long userId, HttpServletResponse response) {
        // 유저 조회
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(()->new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

        // OAuth 정보 조회
        Oauth oauth = authRepository.findByUserId(userId)
            .orElseThrow(() -> new UnauthorizedException(ErrorMessage.OAUTH_NOT_FOUND));

        // Kakao Unlink 호출
        try {
            kakaoOauthClient.unlinkUser(oauth.getProviderId());
        } catch (Exception e) {
            log.error(">>>>> Kakao unlink 실패: {}", e.getMessage());
        }

        // 문서 soft Delete 처리
        try {
            int updated = documentRepository.softDeleteByUserId(userId, LocalDateTime.now());
            if (updated == 0) {
                log.warn("회원 탈퇴 - soft delete 대상 문서가 없음, userId={}", userId);
            }
        } catch (Exception e) {
            log.error("회원 탈퇴 - 문서 soft delete 중 오류, userId={}", userId, e);
            throw new RuntimeException("회원 탈퇴 - 문서 삭제 중 오류 발생");
        }

        // 유저 정보 Soft delete 처리
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        // 쿠키 삭제 처리
        cookieUtil.invalidateCookie(response, "access_token");
        cookieUtil.invalidateCookie(response, "refresh_token");
    }
}
