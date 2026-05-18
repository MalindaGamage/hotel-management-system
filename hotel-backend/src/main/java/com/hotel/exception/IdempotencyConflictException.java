package com.hotel.exception;

public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException(String key) {
        super("Payment with idempotency key '" + key + "' already exists");
    }
}
