package com.example.bookmarket.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Date;

public record LoanDto(
        @NotBlank(message = "id can not blank") Long userId,
        @NotBlank(message = "id can not blank") Long bookId,
        Date dueDate
) {}
