package com.example.bookmarket.exception;

public class BookAlreadyReturnedException extends RuntimeException {
    public BookAlreadyReturnedException() {
        super("کتاب قبلاً برگشت داده شده است.");
    }
}
