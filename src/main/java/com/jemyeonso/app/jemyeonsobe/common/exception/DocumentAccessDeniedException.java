package com.jemyeonso.app.jemyeonsobe.common.exception;

import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;

public class DocumentAccessDeniedException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public DocumentAccessDeniedException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = null;
    }

    public DocumentAccessDeniedException(String message) {
        super(message);
        this.errorMessage = null;
    }
}