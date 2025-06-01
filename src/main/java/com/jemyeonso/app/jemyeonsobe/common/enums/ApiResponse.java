package com.jemyeonso.app.jemyeonsobe.common.enums;

import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private String code;
    private String message;
    private T data;

    // enum 기반 성공 응답
    public static <T> ApiResponse<T> success(ApiResponseCode responseCode, String message, T data) {
        return new ApiResponse<>(responseCode.getCode(), message, data);
    }

    public static <T> ApiResponse<T> success(ApiResponseCode responseCode, T data) {
        return new ApiResponse<>(responseCode.getCode(), getDefaultMessage(responseCode), data);
    }

    public static ApiResponse<Void> success(ApiResponseCode responseCode, String message) {
        return new ApiResponse<>(responseCode.getCode(), message, null);
    }

    // enum 기반 에러 응답
    public static <T> ApiResponse<T> error(ApiResponseCode responseCode, String message) {
        return new ApiResponse<>(responseCode.getCode(), message, null);
    }

    public static <T> ApiResponse<T> error(ApiResponseCode responseCode) {
        return new ApiResponse<>(responseCode.getCode(), getDefaultMessage(responseCode), null);
    }

    // 커스텀 에러 (하위 호환용)
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    // 기본 메시지 매핑
    private static String getDefaultMessage(ApiResponseCode responseCode) {
        switch (responseCode) {
            case FILE_GET_SUCCESS: return "파일 조회에 성공하였습니다.";
            case FILE_LIST_SUCCESS: return "파일 목록 조회에 성공하였습니다.";
            case FILE_DELETE_SUCCESS: return "파일이 삭제되었습니다.";
            case INTERVIEW_LIST_GET_SUCCESS: return "면접 목록 조회에 성공하였습니다.";
            case FILE_NOT_FOUND: return "파일을 찾을 수 없습니다.";
            case FILE_ACCESS_DENIED: return "파일에 접근할 권한이 없습니다.";
            case INTERVIEW_NOT_FOUND: return "면접을 찾을 수 없습니다.";
            case PAGINATION_INVALID_PARAMETER: return "유효하지 않은 페이지네이션 파라미터입니다.";
            case INTERNAL_ERROR: return "서버 오류가 발생했습니다.";
            case UNAUTHORIZED: return "인증이 필요합니다.";
            default: return "요청이 처리되었습니다.";
        }
    }
}