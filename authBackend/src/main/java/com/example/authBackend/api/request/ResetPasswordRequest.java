package com.example.authBackend.api.request;

public record ResetPasswordRequest(
        String email,
        String rewrittenPassword,
        String password
) {}