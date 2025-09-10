package com.jemyeonso.app.jemyeonsobe.util;

import com.jemyeonso.app.jemyeonsobe.api.auth.service.TokenBlacklistService;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration; // 30분 (1800000ms)

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // 7일 (604800000ms)

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * accessToken 생성
     *
     * @param user
     * @return
     */
    public String createAccessToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * refreshToken 생성
     * @param user
     * @return
     */
    public String createRefreshToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰 유효성 검증 (블랙리스트 확인 포함)
     */

    public boolean isValidToken(String token) {
        try {
            // 1. JWT 서명 및 만료 확인
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            // 2. 토큰 타입에 따른 블랙리스트 확인
            String tokenType = getTokenType(token);
            if ("access".equals(tokenType)) {
                if (tokenBlacklistService.isAccessTokenBlacklisted(token)) {
                    log.warn("블랙리스트에 등록된 Access Token입니다.");
                    return false;
                }
            } else if ("refresh".equals(tokenType)) {
                if (tokenBlacklistService.isRefreshTokenBlacklisted(token)) {
                    log.warn("블랙리스트에 등록된 Refresh Token입니다.");
                    return false;
                }
            }

            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰입니다: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 토큰입니다: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 토큰입니다: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("잘못된 서명의 토큰입니다: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 토큰입니다: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서부터 userId 추출
     * @param token
     * @return
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            log.error("토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
            throw new RuntimeException("토큰에서 사용자 ID를 추출할 수 없습니다.");
        }
    }

    /**
     * 토큰의 남은 만료 시간을 반환 (밀리초)
     */
    public long getRemainingExpiration(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            long expirationTime = claims.getExpiration().getTime();
            long currentTime = System.currentTimeMillis();

            return Math.max(0, expirationTime - currentTime);
        } catch (Exception e) {
            log.error("토큰 만료 시간 계산 실패: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 토큰의 만료 시간을 반환
     */
    public long getExpirationTime(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration().getTime();
        } catch (Exception e) {
            log.error("토큰 만료 시간 추출 실패: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 토큰 타입 확인 (access/refresh)
     */
    public String getTokenType(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return (String) claims.get("type");
        } catch (Exception e) {
            log.error("토큰 타입 추출 실패: {}", e.getMessage());
            return null;
        }
    }
}
