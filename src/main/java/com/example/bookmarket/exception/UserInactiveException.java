package com.example.bookmarket.exception;

public class UserInactiveException extends RuntimeException {

    public UserInactiveException(Long userId) {
        super("User with ID " + userId + " is inactive.");
    }

    public UserInactiveException(String username) {
        super("User '" + username + "' is inactive.");
    }

    public UserInactiveException(String message, boolean isCustomMessage) {
        super(message);
    }

    public UserInactiveException() {
        super("User account is not active.");
    }
}