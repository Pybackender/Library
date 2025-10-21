package com.example.bookmarket.dto;

public record StatisticsDto(
        Long totalBooks,
        Long totalUsers,
        Long totalLoans,
        Long activeLoans,
        Long returnedLoans
) {
}