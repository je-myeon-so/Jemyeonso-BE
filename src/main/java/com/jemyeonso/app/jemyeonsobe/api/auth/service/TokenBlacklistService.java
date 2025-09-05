package com.jemyeonso.app.jemyeonsobe.api.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String ACCESS_BLACKLIST_PREFIX = "blacklist:access:";
    private static final String REFRESH_BLACKLIST_PREFIX = "blacklist:refresh:";

    public void blacklistAccessToken(String accessToken, long remainingExpiration) {
        try {
            String key = ACCESS_BLACKLIST_PREFIX + accessToken;

            if (remainingExpiration > 0) {
                redisTemplate.opsForValue().set(key, "blacklisted", remainingExpiration, TimeUnit.MILLISECONDS);
                log.info("Access Token이 블랙리스트에 추가되었습니다. TTL: {}ms", remainingExpiration);
            } else {
                log.warn("이미 만료된 Access Token입니다. 블랙리스트에 추가하지 않습니다.");
            }
        } catch (Exception e) {
            log.error("Access Token 블랙리스트 추가 실패: {}", e.getMessage(), e);
        }
    }

    public void blacklistRefreshToken(String refreshToken, long remainingExpiration) {
        try {
            String key = REFRESH_BLACKLIST_PREFIX + refreshToken;

            if (remainingExpiration > 0) {
                redisTemplate.opsForValue().set(key, "blacklisted", remainingExpiration, TimeUnit.MILLISECONDS);
                log.info("Refresh Token이 블랙리스트에 추가되었습니다. TTL: {}ms", remainingExpiration);
            } else {
                log.warn("이미 만료된 Refresh Token입니다. 블랙리스트에 추가하지 않습니다.");
            }
        } catch (Exception e) {
            log.error("Refresh Token 블랙리스트 추가 실패: {}", e.getMessage(), e);
        }
    }

    public boolean isAccessTokenBlacklisted(String accessToken) {
        try {
            String key = ACCESS_BLACKLIST_PREFIX + accessToken;
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Access Token 블랙리스트 확인 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean isRefreshTokenBlacklisted(String refreshToken) {
        try {
            String key = REFRESH_BLACKLIST_PREFIX + refreshToken;
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Refresh Token 블랙리스트 확인 실패: {}", e.getMessage(), e);
            return false;
        }
    }
}
