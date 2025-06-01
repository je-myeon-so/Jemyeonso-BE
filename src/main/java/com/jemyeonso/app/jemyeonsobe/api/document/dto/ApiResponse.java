package com.jemyeonso.app.jemyeonsobe.api.document.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("FILE_GET_SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static ApiResponse<Void> successDelete(String message) {
        return new ApiResponse<>("FILE_DELETE_SUCCESS", message, null);
    }

    public static <T> ApiResponse<List<T>> successList(String message, List<T> data) {
        return new ApiResponse<>("FILE_LIST_SUCCESS", message, data);
    }
}