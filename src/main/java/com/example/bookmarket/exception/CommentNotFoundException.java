package com.example.bookmarket.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(Long commentId) {
        super("Comment not found with ID: " + commentId);
    }
}