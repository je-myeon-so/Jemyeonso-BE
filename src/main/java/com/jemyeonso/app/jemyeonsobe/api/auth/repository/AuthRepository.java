package com.jemyeonso.app.jemyeonsobe.api.auth.repository;

import com.jemyeonso.app.jemyeonsobe.api.auth.entity.Oauth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface AuthRepository extends JpaRepository<Oauth, Long> {
    Optional<Oauth> findByProviderAndProviderId(String provider, String providerId);

    Optional<Oauth> findByUserId(Long userId);

    // 사용자 ID로 refresh token을 갱신
    @Transactional
    @Modifying
    @Query("UPDATE Oauth o SET o.refreshToken = :refreshToken WHERE o.user.id = :userId")
    void updateRefreshToken(Long userId, String refreshToken);
}
