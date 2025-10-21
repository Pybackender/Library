package com.example.bookmarket.exception;

public class LimitExceededException extends RuntimeException {
    public LimitExceededException(Long userId) {
        super("User with ID " + userId + " has exceeded the limit of active loans.");
    }
}