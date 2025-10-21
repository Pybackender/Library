package com.example.bookmarket.exception;

public class UserInactiveException extends RuntimeException {
    public UserInactiveException(Long userId) {
        super("User with ID " + userId + " is inactive and cannot create a loan.");
    }
}