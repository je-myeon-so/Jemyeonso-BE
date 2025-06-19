package com.jemyeonso.app.jemyeonsobe.common.exception;

import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;

public class ForbiddenException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public ForbiddenException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = null;
    }

    public ForbiddenException(String message) {
        super(message);
        this.errorMessage = null;
    }
}