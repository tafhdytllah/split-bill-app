package com.tafhdev.split_bill_app.shared.application.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class IdempotencyHashGenerator {

    public String generate(String request) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            request.getBytes(StandardCharsets.UTF_8)
                    );

            return toHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private String toHex(byte[] hash) {
        StringBuilder result = new StringBuilder(hash.length * 2);

        for (byte value : hash) {
            result.append(String.format("%02x", value));
        }

        return result.toString();
    }

}
