package com.example.authBackend.service;

import com.example.authBackend.api.request.LoginRequest;
import com.example.authBackend.api.response.TokenResponse;
import com.example.authBackend.api.request.RefreshTokenRequest;
import com.example.authBackend.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    UserDTO registerUser(UserDTO userDTO);
    TokenResponse loginUser(LoginRequest loginRequest, HttpServletResponse response);

    TokenResponse refreshToken(RefreshTokenRequest body, HttpServletRequest request, HttpServletResponse response);

    ResponseEntity<String> verifyEmailAndSendOtp(String email);

    ResponseEntity<String> verifyOtp(String email, String otp);

    ResponseEntity<String> resetPassword(String email, String rewrittenPassword, String password);

    void logout(HttpServletRequest request, HttpServletResponse response);
}
