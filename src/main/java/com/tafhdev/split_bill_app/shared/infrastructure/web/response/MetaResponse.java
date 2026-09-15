package com.tafhdev.split_bill_app.shared.infrastructure.web.response;

public record MetaResponse(
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean hasNext,
        boolean hasPrevious
) {
}
