package com.example.bookmarket.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record LoanDto(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Book ID is required")
        Long bookId,

        @NotNull(message = "Due date is required")
        LocalDate dueDate
) {}