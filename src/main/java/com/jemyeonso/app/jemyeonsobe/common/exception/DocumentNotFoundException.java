package com.jemyeonso.app.jemyeonsobe.common.exception;

import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;

public class DocumentNotFoundException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public DocumentNotFoundException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = null;
    }

    public DocumentNotFoundException(String message) {
        super(message);
        this.errorMessage = null;
    }
}