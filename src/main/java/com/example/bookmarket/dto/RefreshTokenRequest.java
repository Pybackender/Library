package com.example.bookmarket.dto;

import lombok.Data;
@Data
public class RefreshTokenRequest {
    private String refreshToken;

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}