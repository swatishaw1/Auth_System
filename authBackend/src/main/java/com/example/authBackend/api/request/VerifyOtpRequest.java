package com.example.authBackend.api.request;

public record VerifyOtpRequest(
        String email,
        String otp
) {
}
