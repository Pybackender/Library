package com.example.bookmarket.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateLibrarianDto(
        Long id,
        @NotBlank(message = "username is mandatory") String username,
        @NotBlank(message = "password is mandatory") String password
) {
}