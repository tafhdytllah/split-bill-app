package com.tafhdev.split_bill_app.shared.domain.exception;

public final class Guard {

    private Guard() {
    }

    public static <T> T requireNotNull(
            T value,
            String fieldName
    ) {
        if (value == null) {
            throw new DomainException(
                    "%s must not be null".formatted(fieldName)
            );
        }

        return value;
    }

    public static String requireNonBlank(
            String value,
            String fieldName
    ) {
        if (value == null) {
            throw new DomainException(
                    "%s must not be null".formatted(fieldName)
            );
        }

        if (value.isBlank()) {
            throw new DomainException(
                    "%s must not be blank".formatted(fieldName)
            );
        }

        return value;
    }
}
