package com.example.authBackend.api.response;

import com.example.authBackend.dto.UserDTO;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    String TokenType,
    UserDTO user
){
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn, UserDTO user){
        return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
    }
}