package com.jemyeonso.app.jemyeonsobe.common.exception;

import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("[BadRequest] {}", ex.getMessage());
        return buildErrorResponse(ApiResponseCode.BAD_REQUEST, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(UnauthorizedException ex) {
        log.warn("[Unauthorized] {}", ex.getMessage());
        return buildErrorResponse(ApiResponseCode.UNAUTHORIZED, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<?>> handleForbidden(ForbiddenException ex) {
        log.warn("[Forbidden] {}", ex.getMessage());
        return buildErrorResponse(ApiResponseCode.FORBIDDEN, ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("[NotFound] {}", ex.getMessage());
        return buildErrorResponse(ApiResponseCode.NOT_FOUND, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        log.warn("[MaxUploadSizeExceeded] {}", ex.getMessage());
        return buildErrorResponse(ApiResponseCode.MAX_UPLOAD_SIZE_EXCEEDED, "파일 크기가 제한을 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleInternal(Exception ex, HttpServletRequest request) {
        if (request.getRequestURI().contains("/v3/api-docs")) {
            return ResponseEntity.ok().build();
        }

        log.error("[InternalError] 요청 URI: {} | Message: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse(ApiResponseCode.INTERNAL_SERVER_ERROR, "서버에서 에러가 발생하였습니다", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponse(ApiResponseCode code, String message, HttpStatus status) {
        return ResponseEntity.status(status)
            .body(ApiResponse.fail(code, message));
    }
}
