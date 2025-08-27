package com.jemyeonso.app.jemyeonsobe.common.enums;

import lombok.Getter;

@Getter
public enum ErrorMessage {
    // 400 (Bad Request)

    // 401 (Unauthorized)
    INVALID_ACCESS_TOKEN("유효하지 않은 access token 입니다."),
    INVALID_REFRESH_TOKEN("유효하지 않은 refresh token 입니다."),
    ACCESS_TOKEN_EXPIRED("만료된 access token 입니다."),
    REFRESH_TOKEN_EXPIRED("만료된 refresh token 입니다."),
    USER_ID_EXTRACTION_FAILED("토큰에서 사용자 ID를 추출할 수 없습니다."),
    OAUTH_NOT_FOUND("OAuth 정보가 존재하지 않습니다."),
    DB_NOT_MATCH("DB에 저장된 값과 일치하지 않습니다."),

    // 403 (Forbidden)
    NO_INTERVIEW_PERMISSION("해당 면접에 접근할 권한이 없습니다."),
    LOGIN_REQUIRED("로그인이 필요합니다."),
    INVALID_USER_INFO("잘못된 사용자 정보입니다."),

    // 404 (Not Found)
    USER_NOT_FOUND("존재하지 않는 사용자입니다."),
    QUESTION_NOT_FOUND("존재하지 않는 질문입니다."),
    INTERVIEW_NOT_FOUND("존재하지 않는 인터뷰입니다.");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(this.message, args);
    }
}
