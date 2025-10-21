package com.example.bookmarket.exception;

public class LoanNotFoundException extends RuntimeException {
    public LoanNotFoundException(Long loanId) {
        super("Loan not found for ID: " + loanId);
    }

    public LoanNotFoundException(String message) {
        super(message);
    }
}
