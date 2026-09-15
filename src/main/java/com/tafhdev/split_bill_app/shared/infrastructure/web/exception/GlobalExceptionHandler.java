package com.tafhdev.split_bill_app.shared.infrastructure.web.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.tafhdev.split_bill_app.shared.application.exception.AccessDeniedException;
import com.tafhdev.split_bill_app.shared.application.exception.AuthenticationException;
import com.tafhdev.split_bill_app.shared.application.exception.ConflictException;
import com.tafhdev.split_bill_app.shared.application.exception.ResourceNotFoundException;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.infrastructure.logger.LogHelper;
import com.tafhdev.split_bill_app.shared.infrastructure.web.response.ApiResponse;
import com.tafhdev.split_bill_app.shared.infrastructure.web.response.ErrorCode;
import com.tafhdev.split_bill_app.shared.infrastructure.web.response.ResponseHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 : DOMAIN_ERROR
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Object>> handleDomain(
            DomainException exception
    ) {
        LogHelper.warn("DOMAIN_ERROR : {}", exception.getMessage());

        return ResponseHelper.error(
                HttpStatus.BAD_REQUEST,
                ErrorCode.DOMAIN_ERROR,
                exception.getMessage()
        );
    }

    // 400 : VALIDATION_ERROR
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> details = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        details.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        LogHelper.warn("VALIDATION_ERROR : {}", details);

        return ResponseHelper.error(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Validation failed",
                details
        );
    }

    // 400 : BAD_REQUEST
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidRequestBody(
            HttpMessageNotReadableException ex
    ) {
        LogHelper.warn("BAD_REQUEST : {}", ex.getMessage());

        Throwable cause = ex;

        while (cause != null) {

            if (cause instanceof UnrecognizedPropertyException unrecognized) {

                return ResponseHelper.error(
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST,
                        "Unknown field: " + unrecognized.getPropertyName()
                );
            }

            cause = cause.getCause();
        }

        return ResponseHelper.error(
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST,
                "Invalid request body"
        );
    }

    // 401 : AUTHENTICATION_FAILED
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthentication(
            AuthenticationException exception
    ) {
        LogHelper.warn("AUTHENTICATION_FAILED : {}", exception.getMessage());

        return ResponseHelper.error(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.AUTHENTICATION_FAILED,
                exception.getMessage()
        );
    }

    // 403 : ACCESS_DENIED
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
            AccessDeniedException exception
    ) {
        LogHelper.warn("ACCESS_DENIED : {}", exception.getMessage());

        return ResponseHelper.error(
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                exception.getMessage()
        );
    }

    // 404 : RESOURCE_NOT_FOUND
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(
            ResourceNotFoundException exception
    ) {
        LogHelper.warn("RESOURCE_NOT_FOUND : {}", exception.getMessage());

        return ResponseHelper.error(
                HttpStatus.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND,
                exception.getMessage()
        );
    }

    // 409 : CONFLICT
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflict(
            ConflictException exception
    ) {
        LogHelper.warn("CONFLICT : {}", exception.getMessage());

        return ResponseHelper.error(
                HttpStatus.CONFLICT,
                ErrorCode.CONFLICT,
                exception.getMessage()
        );
    }

    // 500 : INTERNAL_SERVER_ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(
            Exception ex
    ) {
        LogHelper.error("INTERNAL_SERVER_ERROR {}", ex.getMessage());

        return ResponseHelper.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Something went wrong"
        );
    }
}
