package com.jemyeonso.app.jemyeonsobe.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
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
    }

    /**
     * 쿠키에서 토큰 추출
     * @param request
     * @return
     */
    public String extractTokenFromCookies(HttpServletRequest request) {
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

    /**
     * 쿠키 무효화
     * @param response
     * @param name
     */
    public void invalidateCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
    }
}
