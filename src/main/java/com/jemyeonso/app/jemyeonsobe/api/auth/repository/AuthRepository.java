package com.jemyeonso.app.jemyeonsobe.api.auth.repository;

import com.jemyeonso.app.jemyeonsobe.api.auth.entity.Oauth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface AuthRepository extends JpaRepository<Oauth, Long> {
    // 사용자 ID로 refresh token을 갱신
    @Transactional
    @Modifying
    @Query("UPDATE Oauth o SET o.refreshToken = :refreshToken WHERE o.user.id = :userId")
    void updateRefreshToken(Long userId, String refreshToken);

    // 현재는 deletedAt이 없으니 그대로, 탈퇴 포함/제외 개념은 User 쪽에서 처리
    Optional<Oauth> findByProviderAndProviderId(String provider, String providerId);

    // userId로도 조회 필요할 수 있음
    Optional<Oauth> findByUserId(Long userId);
}
