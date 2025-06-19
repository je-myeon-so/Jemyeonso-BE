package com.jemyeonso.app.jemyeonsobe.util;

import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;
import com.jemyeonso.app.jemyeonsobe.common.exception.ForbiddenException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new ForbiddenException(ErrorMessage.LOGIN_REQUIRED);
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof Long)) {
            throw new ForbiddenException(ErrorMessage.INVALID_USER_INFO);
        }

        return (Long) principal;
    }
}
