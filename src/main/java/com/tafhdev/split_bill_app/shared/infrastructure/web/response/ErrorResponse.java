package com.tafhdev.split_bill_app.shared.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        ErrorCode code,
        String message,
        Map<String, String> details
) {
}
