package com.example.authBackend.api.response;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record ApiErrorResponse (
        int status,
        String error,
        String message,
        String path,
        OffsetDateTime timestamp
){
    public static ApiErrorResponse of(int status, String error, String message, String path){
        return new ApiErrorResponse(status, error, message, path, OffsetDateTime.now(ZoneOffset.UTC));
    }
    public static ApiErrorResponse of(int status, String error, String message, String path,boolean notTimeDate){
        return new ApiErrorResponse(status, error, message, path, null);
    }
}
//ZoneOffset.UTC for timezone