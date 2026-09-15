package com.tafhdev.split_bill_app.shared.infrastructure.web.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public final class ResponseHelper {

    private ResponseHelper() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(
                ApiResponse.success(data)
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(
                ApiResponse.success(data, message)
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, MetaResponse meta) {
        return ResponseEntity.ok(
                ApiResponse.success(data, meta)
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, message));
    }

    public static ResponseEntity<ApiResponse<Object>> error(
            HttpStatus status,
            ErrorCode code,
            String message,
            Map<String, String> details
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                code,
                message,
                details
        );

        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(errorResponse));
    }

    public static ResponseEntity<ApiResponse<Object>> error(
            HttpStatus status,
            ErrorCode code,
            String message
    ) {
        return error(
                status,
                code,
                message,
                null
        );
    }
}
