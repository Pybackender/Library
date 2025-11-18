package com.example.bookmarket.dto;

public record TokenDto(
        String accessToken,
        String refreshToken,
        String message
) {
    // Constructor برای backward compatibility
    public TokenDto(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, null);
    }

    // Constructor فقط با accessToken و refreshToken (برای لاگین)
    public static TokenDto of(String accessToken, String refreshToken) {
        return new TokenDto(accessToken, refreshToken, null);
    }

    // Constructor کامل
    public static TokenDto of(String accessToken, String refreshToken, String message) {
        return new TokenDto(accessToken, refreshToken, message);
    }
}