package com.tafhdev.split_bill_app.shared.application.service;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class IdempotencyRequestBuilder {

    private IdempotencyRequestBuilder() {
    }

    public static String build(Object... values) {
        return Arrays.stream(values)
                .map(String::valueOf)
                .collect(Collectors.joining("|"));
    }
}
