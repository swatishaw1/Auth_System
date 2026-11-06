package com.example.authBackend.service;

import com.example.authBackend.service.implementation.AuthenticationServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        // Arrange: Create the actual service using the mocked AuthenticationManager.
        authenticationService = new AuthenticationServiceImpl(authenticationManager);
    }

    @Test
    void authenticate_shouldAuthenticateSuccessfully() {

        // Arrange: Mock successful authentication.
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        // Act: Authenticate with valid credentials.
        assertDoesNotThrow(() ->
                authenticationService.authenticate("test@gmail.com", "password123")
        );

        // Verify: AuthenticationManager was called once.
        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_shouldPassCorrectCredentials() {

        // Arrange: Capture the authentication token.
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        // Act: Authenticate using supplied credentials.
        authenticationService.authenticate("test@gmail.com", "password123");

        // Verify: Capture and inspect the credentials passed to Spring Security.
        verify(authenticationManager).authenticate(captor.capture());

        assertEquals("test@gmail.com", captor.getValue().getPrincipal());
        assertEquals("password123", captor.getValue().getCredentials());
    }

    @Test
    void authenticate_shouldThrowBadCredentialsWhenAuthenticationFails() {

        // Arrange: Simulate Spring Security rejecting authentication.
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act and Assert: Verify that authentication failure is propagated.
        assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.authenticate("test@gmail.com", "wrongPassword")
        );

        // Verify: AuthenticationManager was called.
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}