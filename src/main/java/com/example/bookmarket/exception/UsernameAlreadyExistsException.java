package com.example.bookmarket.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super(".نام کاربری '" + username + "' قبلا استفاده شده");
    }
}