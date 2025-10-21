package com.example.bookmarket.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
    }

    public UserNotFoundException(String username) {
        super("User not found with username: " + username);
    }
}
