package com.jemyeonso.app.jemyeonsobe.util;

import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;


@Slf4j
@Component
public class JwtTokenProvider {
    @Value("${JWT_SECRET}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * accessToken 생성
     *
     * @param user
     * @return
     */
    public String createAccessToken(User user) {
        long accessTokenValidity = 1000 * 60 * 30; // 30분
        return Jwts.builder()
            .setSubject(user.getId().toString())
            .claim("email", user.getEmail())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
            .signWith(key)
            .compact();
    }

    /**
     * refreshToken 생성
     * @param user
     * @return
     */
    public String createRefreshToken(User user) {
        long refreshTokenValidity = 1000L * 60 * 60 * 24 * 14; // 14일
        return Jwts.builder()
            .setSubject(user.getId().toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
            .signWith(key)
            .compact();
    }


    /**
     * 토큰 검증
     *
     * @param token
     * @return
     */

    public boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Unknown error parsing JWT: {}", e.getMessage());
        }
        return false;
    }


    /**
     * 토큰에서부터 userId 추출
     * @param token
     * @return
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token)
            .getBody();
        return Long.valueOf(claims.getSubject());
    }
}