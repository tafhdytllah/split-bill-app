package com.tafhdev.split_bill_app.shared.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        T data,
        String message,
        MetaResponse meta,
        ErrorResponse errors
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null, null, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message, null, null);
    }

    public static <T> ApiResponse<T> success(T data, MetaResponse meta) {
        return new ApiResponse<>(data, null, meta, null);
    }

    public static ApiResponse<Object> error(ErrorResponse errors) {
        return new ApiResponse<>(null, null, null, errors);
    }
}
