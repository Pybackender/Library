package com.example.bookmarket.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserDto(
        Long userId,
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "password is required you can type your last password or change it.")String password, // Optional, can be null if not updating
        String nickname  // Optional, can be null if not updating
) {
}
