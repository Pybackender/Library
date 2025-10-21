package com.example.bookmarket.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record AddUserDto(
        @NotBlank(message = "username is mandatory") String username,
        @NotBlank(message = "password is mandatory") String password,
        String nickname
)
{

}
