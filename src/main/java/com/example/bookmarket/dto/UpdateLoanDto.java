package com.example.bookmarket.dto;

import java.time.LocalDate;

public record UpdateLoanDto(
        Long id,
        Long userId,
        Long bookId,
        LocalDate dueDate
) {
}
