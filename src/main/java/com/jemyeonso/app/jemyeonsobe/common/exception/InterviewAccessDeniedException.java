package com.jemyeonso.app.jemyeonsobe.common.exception;

import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;

public class InterviewAccessDeniedException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public InterviewAccessDeniedException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = null;
    }

    public InterviewAccessDeniedException(String message) {
        super(message);
        this.errorMessage = null;
    }
}
