package com.example.authBackend.service;

import com.example.authBackend.api.request.LoginRequest;
import com.example.authBackend.api.response.TokenResponse;
import com.example.authBackend.api.request.RefreshTokenRequest;
import com.example.authBackend.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    UserDTO registerUser(UserDTO userDTO);
    TokenResponse loginUser(LoginRequest loginRequest, HttpServletResponse response);

    TokenResponse refreshToken(RefreshTokenRequest body, HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);
}
