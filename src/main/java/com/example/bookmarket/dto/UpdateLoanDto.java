package com.example.bookmarket.dto;

import java.util.Date;

public record UpdateLoanDto(
        Long id,
        Long userId,
        Long bookId,
        Date dueDate
) {
}
