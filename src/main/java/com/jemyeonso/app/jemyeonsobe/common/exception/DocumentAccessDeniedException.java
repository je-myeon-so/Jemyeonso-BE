package com.jemyeonso.app.jemyeonsobe.common.exception;

public class DocumentAccessDeniedException extends RuntimeException {
    public DocumentAccessDeniedException(String message) {
        super(message);
    }
}