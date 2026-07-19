package com.example.authBackend.api.response;
import org.springframework.http.HttpStatus;

public record ErrorResponse(
    String message,
    HttpStatus status){}