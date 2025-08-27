package com.jemyeonso.app.jemyeonsobe.common.exception;

import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;

public class ResourceNotFoundException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public ResourceNotFoundException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = null;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorMessage = null;
    }
}
