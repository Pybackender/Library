package com.example.bookmarket.exception;

public class UserAlreadyLoggedOutException extends RuntimeException {
    public UserAlreadyLoggedOutException(String username) {
        super(".قبلا خارج شده " +" "+ username +" "+ "یوزر ");
    }
}