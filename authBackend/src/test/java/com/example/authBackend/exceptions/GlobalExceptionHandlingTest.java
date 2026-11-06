
package com.example.authBackend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlingTest {

    private GlobalExceptionHandling exceptionHandling;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandling = new GlobalExceptionHandling();

        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/auth/login");
    }

    // 1. Username not found
    @Test
    void shouldReturn401WhenUsernameNotFound() {

        UsernameNotFoundException exception =
                new UsernameNotFoundException("User not found");

        var response =
                exceptionHandling.handleException(exception, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().status());
        assertEquals("UNAUTHORIZED", response.getBody().error());
        assertEquals("User not found", response.getBody().message());
        assertEquals("/api/v1/auth/login", response.getBody().path());
    }

    // 2. Bad credentials
    @Test
    void shouldReturn401WhenCredentialsAreInvalid() {

        BadCredentialsException exception =
                new BadCredentialsException("Invalid credentials");

        var response =
                exceptionHandling.handleException(exception, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UNAUTHORIZED", response.getBody().error());
        assertEquals("Invalid credentials", response.getBody().message());
    }

    // 3. Credentials expired
    @Test
    void shouldReturn401WhenCredentialsAreExpired() {

        CredentialsExpiredException exception =
                new CredentialsExpiredException("Credentials expired");

        var response =
                exceptionHandling.handleException(exception, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Credentials expired", response.getBody().message());
    }

    // 4. Disabled user
    @Test
    void shouldReturn401WhenUserIsDisabled() {

        DisabledException exception =
                new DisabledException("Account is disabled");

        var response =
                exceptionHandling.handleException(exception, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Account is disabled", response.getBody().message());
    }

    // 5. Resource not found
    @Test
    void shouldReturn404WhenResourceIsNotFound() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException("User not found");

        var response =
                exceptionHandling.handleResourceNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().message());
        assertEquals(HttpStatus.NOT_FOUND, response.getBody().status());
    }

    // 6. Illegal argument
    @Test
    void shouldReturn400WhenArgumentIsInvalid() {

        IllegalArgumentException exception =
                new IllegalArgumentException("Invalid input");

        var response =
                exceptionHandling.handleIllegalAccessException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid input", response.getBody().message());
        assertEquals(HttpStatus.BAD_REQUEST, response.getBody().status());
    }
}
