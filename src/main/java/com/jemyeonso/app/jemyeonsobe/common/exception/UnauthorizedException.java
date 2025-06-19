package com.jemyeonso.app.jemyeonsobe.common.exception;

import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;

public class UnauthorizedException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public UnauthorizedException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = null;
    }

    public UnauthorizedException(String message) {
        super(message);
        this.errorMessage = null;
    }
}
