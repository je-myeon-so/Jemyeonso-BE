package com.jemyeonso.app.jemyeonsobe.api.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
@Table(name = "oauth")
public class Oauth {
    @Id
    @Column(name = "id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId

    @JoinColumn(name = "id")
    private User user;

    // 서비스 제공자가 제공하는 ID
    @Column(name = "provider_id", nullable = false, unique = true, length = 100)
    private String providerId;

    // 서비스 제공자의 종류 (ex. Kakao, Google)
    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    // 리프레시 토큰 저장
    @Column(name = "refresh_token", length = 512)
    private String refreshToken;
}
