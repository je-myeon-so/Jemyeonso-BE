package com.jemyeonso.app.jemyeonsobe.api.user.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import com.jemyeonso.app.jemyeonsobe.api.auth.entity.Oauth;
import com.jemyeonso.app.jemyeonsobe.api.auth.repository.AuthRepository;
import com.jemyeonso.app.jemyeonsobe.api.auth.service.KakaoOauthClient;
import com.jemyeonso.app.jemyeonsobe.api.document.repository.DocumentRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserFeedbackResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserInfoResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserRepository;
import com.jemyeonso.app.jemyeonsobe.common.exception.ResourceNotFoundException;
import com.jemyeonso.app.jemyeonsobe.common.exception.UnauthorizedException;
import com.jemyeonso.app.jemyeonsobe.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private KakaoOauthClient kakaoOauthClient;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private DocumentRepository documentRepository;

    // Fixture
    private User activeUser() {
        return User.builder()
            .id(1L)
            .email("test@example.com")
            .name("사용자")
            .nickname("사용자")
            .profileImgUrl("http://img")
            .createdAt(LocalDateTime.now())
            .deletedAt(null)
            .build();
    }

    private Oauth oauth(User u) {
        return Oauth.builder()
            .user(u)
            .provider("KAKAO")
            .providerId("1234567890")
            .refreshToken("dummy-refresh-token")
            .build();
    }


    @Nested
    @DisplayName("사용자 정보 조회")
    class getUserInfoTest {
        @Test
        void getUserInfo_성공() {
            // given
            User u = activeUser();
            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(u));

            // when
            UserInfoResponseDto dto = userService.getUserInfo(1L);

            // then
            assertEquals(u.getId(), dto.getUserId());
            assertEquals(u.getNickname(), dto.getNickname());
            assertEquals(u.getEmail(), dto.getEmail());
        }

        @Test
        void getUserInfo_예외() {
            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> userService.getUserInfo(1L));
        }
    }

    @Nested
    @DisplayName("사용자 정보 수정")
    class patchUserInfoTest {
        @Test
        void patchUserInfo_일부필드수정_성공() {
            User u = activeUser();
            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(u));

            UserInfoResponseDto dto = userService.patchUserInfo(1L, "새닉", null, "한마디!");

            assertEquals("새닉", dto.getNickname());
            assertEquals("한마디!", dto.getComment());
            assertEquals("http://img", dto.getProfileImgUrl()); // null 전달 → 기존 유지
        }

        @Test
        void  patchUserInfo_존재하지않는사용자_예외발생() {
            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                () -> userService.patchUserInfo(1L, "a", "b", "c"));
        }
    }


    @Nested
    @DisplayName("개선점 조회")
    class getImprovementTest {
        @Test
        void getImprovement_값없음_null허용() {
            User u = activeUser();
            u.setImprovement(null);
            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(u));

            UserFeedbackResponseDto dto = userService.getImprovement(1L);
            assertNull(dto.getFeedback()); // null 허용
        }

        @Test
        void getImprovement_존재하지않는사용자_예외발생() {
            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> userService.getImprovement(1L));
        }
    }


    @Nested
    @DisplayName("사용자 탈퇴")
    class deleteUserTest {
        @Test
        void deleteUser_정상조건_탈퇴성공() {
            // given
            User u = activeUser();
            Oauth o = oauth(u);
            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(u));
            when(authRepository.findByUserId(1L)).thenReturn(Optional.of(o));
            when(documentRepository.softDeleteByUserId(eq(1L), any(LocalDateTime.class))).thenReturn(3);

            HttpServletResponse resp = mock(HttpServletResponse.class);

            // when
            userService.deleteUser(1L, resp);

            // then
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertNotNull(captor.getValue().getDeletedAt());

            verify(documentRepository).softDeleteByUserId(eq(1L), any(LocalDateTime.class));

            verify(cookieUtil).invalidateCookie(resp, "access_token");
            verify(cookieUtil).invalidateCookie(resp, "refresh_token");

            verify(kakaoOauthClient).unlinkUser(o.getProviderId());
        }

        @Test
        void deleteUser_kakaoUnlink실패_탈퇴계속진행() {
            User u = activeUser();
            Oauth o = oauth(u);
            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(u));
            when(authRepository.findByUserId(1L)).thenReturn(Optional.of(o));
            doThrow(new RuntimeException("kakao down")).when(kakaoOauthClient).unlinkUser(anyString());
            when(documentRepository.softDeleteByUserId(eq(1L), any(LocalDateTime.class))).thenReturn(0);

            HttpServletResponse resp = mock(HttpServletResponse.class);

            // when & then
            assertDoesNotThrow(() -> userService.deleteUser(1L, resp));
            verify(userRepository).save(any(User.class)); // 탈퇴는 진행
            verify(cookieUtil).invalidateCookie(resp, "access_token");
            verify(cookieUtil).invalidateCookie(resp, "refresh_token");
        }

        @Test
        void deleteUser_oauth없음_예외발생() {
            User u = activeUser();
            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(u));
            when(authRepository.findByUserId(1L)).thenReturn(Optional.empty());

            HttpServletResponse resp = mock(HttpServletResponse.class);

            assertThrows(UnauthorizedException.class, () -> userService.deleteUser(1L, resp));

            verify(documentRepository, never()).softDeleteByUserId(anyLong(), any());
            verify(userRepository, never()).save(any());
            verify(cookieUtil, never()).invalidateCookie(any(), anyString());
        }

        @Test
        void deleteUser_존재하지않는사용자_예외발생() {
            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
            HttpServletResponse resp = mock(HttpServletResponse.class);
            assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L, resp));
        }

        @Test
        void deleteUser_documentSoftDelete실패_프로세스중단() {
            User u = activeUser();
            Oauth o = oauth(u);
            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(u));
            when(authRepository.findByUserId(1L)).thenReturn(Optional.of(o));
            doNothing().when(kakaoOauthClient).unlinkUser(anyString());
            when(documentRepository.softDeleteByUserId(eq(1L), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("DB error"));

            HttpServletResponse resp = mock(HttpServletResponse.class);

            assertThrows(RuntimeException.class, () -> userService.deleteUser(1L, resp));

            verify(userRepository, never()).save(any());
            verify(cookieUtil, never()).invalidateCookie(any(), anyString());
        }
    }
}
