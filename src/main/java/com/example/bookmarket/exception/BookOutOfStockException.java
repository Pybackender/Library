package com.example.bookmarket.exception;

public class BookOutOfStockException extends RuntimeException {
    public BookOutOfStockException(Long bookId) {
        super("Book with id " + bookId + " is out of stock");
    }
}