package com.example.authBackend.exceptions;


import com.example.authBackend.api.response.ApiErrorResponse;
import com.example.authBackend.api.response.ErrorResponse;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandling {

    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandling.class);

    @ExceptionHandler({
            UsernameNotFoundException.class,
            BadCredentialsException.class,
            CredentialsExpiredException.class,
            DisabledException.class
    })
    public ResponseEntity<ApiErrorResponse> handleException(Exception e, HttpServletRequest request) {
        ApiErrorResponse apiResponse = ApiErrorResponse.of(HttpStatus.UNAUTHORIZED.value(),"UNAUTHORIZED",e.getMessage(),request.getRequestURI());
        logger.error("Exception: {}",e.getClass().getName());
        return new ResponseEntity<>(apiResponse,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception){
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalAccessException(IllegalArgumentException exception){
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Map<String, String>> handleRateLimit(
            RequestNotPermitted exception) {

        Map<String, String> response = Map.of(
                "error", "TOO_MANY_REQUESTS",
                "message", "Too many requests. Please try again later."
        );

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(response);
    }


}
