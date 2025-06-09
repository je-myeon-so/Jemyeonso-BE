package com.jemyeonso.app.jemyeonsobe.util;

import com.jemyeonso.app.jemyeonsobe.common.exception.ForbiddenException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new ForbiddenException("로그인이 필요합니다.");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof Long)) {
            throw new ForbiddenException("잘못된 사용자 정보입니다.");
        }

        return (Long) principal;
    }
}
