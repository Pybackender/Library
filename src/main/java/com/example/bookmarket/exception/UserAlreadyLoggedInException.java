package com.example.bookmarket.exception;

public class UserAlreadyLoggedInException extends RuntimeException {
    public UserAlreadyLoggedInException(String username) {
        super(" .قبلا وارد شده" +" "+ username +" "+ "یوزر");
    }
}