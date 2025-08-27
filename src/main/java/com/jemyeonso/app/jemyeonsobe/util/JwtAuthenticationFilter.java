package com.jemyeonso.app.jemyeonsobe.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        // 스웨거 관련 경로와 인증 경로는 필터 적용 안함
        return requestURI.equals("/api/backend/v1/auth/refresh") ||
                requestURI.startsWith("/swagger-ui/") ||
                requestURI.startsWith("/v3/api-docs/") ||
                requestURI.equals("/v3/api-docs") ||
                requestURI.equals("/swagger-ui.html") ||
                requestURI.startsWith("/swagger-resources/") ||
                requestURI.startsWith("/api/backend/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        // 쿠키에서 access_token 추출
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 토큰이 있을 때만 인증 처리
        if (token != null && !token.trim().isEmpty()) {
            try {
                if (!jwtTokenProvider.isValidToken(token)) {
                    writeForbiddenResponse(response, "만료된 access token입니다.");
                    return;
                }

                Long userId = jwtTokenProvider.getUserIdFromToken(token);

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                System.out.println("JWT parsing failed: " + e.getMessage());
                writeForbiddenResponse(response, "토큰 인증에 실패했습니다: " + e.getMessage());
                return;
            }
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }


    private void writeForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        // 원하는 포맷에 맞춰 응답 JSON 작성 (예: ApiResponse 스타일)
        String json = String.format("{\"code\":\"FORBIDDEN\",\"message\":\"%s\"}", message);
        response.getWriter().write(json);
    }
}
