package com.example.authBackend.controller;

import com.example.authBackend.api.request.LoginRequest;
import com.example.authBackend.api.response.TokenResponse;
import com.example.authBackend.dto.UserDTO;
import com.example.authBackend.exceptions.GlobalExceptionHandling;
import com.example.authBackend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandling())
                .build();
    }

    // 1. Successful registration
    @Test
    void registerUser_shouldReturn201WhenRegistrationIsSuccessful() throws Exception {

        UserDTO response = UserDTO.builder()
                .name("Swati")
                .email("swati@example.com")
                .build();

        when(authService.registerUser(any(UserDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Swati",
                                    "email": "swati@example.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Swati"))
                .andExpect(jsonPath("$.email")
                        .value("swati@example.com"));

        verify(authService, times(1))
                .registerUser(any(UserDTO.class));
    }

    // 2. Malformed JSON
    @Test
    void registerUser_shouldReturn400ForMalformedJson()
            throws Exception {

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Swati",
                                    "email":
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .registerUser(any(UserDTO.class));
    }

    // 3. Empty request body
    @Test
    void registerUser_shouldReturn400ForEmptyRequestBody()
            throws Exception {

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .registerUser(any(UserDTO.class));
    }

    // 4. Unsupported HTTP method
    @Test
    void registerUser_shouldReturn405ForGetRequest()
            throws Exception {

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders
                                .get("/api/v1/auth/register"))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(authService);
    }


    // 1. Successful login
    @Test
    void loginUser_shouldReturn200WhenLoginIsSuccessful() throws Exception {

        TokenResponse tokenResponse = mock(TokenResponse.class);

        when(authService.loginUser(
                any(LoginRequest.class),
                any(HttpServletResponse.class)
        )).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "swati@example.com",
                                "password": "password123"
                            }
                            """))
                .andExpect(status().isOk());

        verify(authService, times(1))
                .loginUser(any(LoginRequest.class), any(HttpServletResponse.class));
    }


    // 2. Malformed JSON
    @Test
    void loginUser_shouldReturn400ForMalformedJson() throws Exception {

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "swati@example.com",
                                "password":
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }


    // 3. Empty request body
    @Test
    void loginUser_shouldReturn400ForEmptyBody() throws Exception {

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }


    // 4. Invalid credentials
    @Test
    void loginUser_shouldPropagateAuthenticationFailure() throws Exception {

        when(authService.loginUser(
                any(LoginRequest.class),
                any(HttpServletResponse.class)
        )).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "swati@example.com",
                                "password": "wrongPassword"
                            }
                            """))
                .andExpect(status().isUnauthorized());

        verify(authService).loginUser(
                any(LoginRequest.class),
                any(HttpServletResponse.class)
        );
    }


    // 5. Unsupported HTTP method
    @Test
    void loginUser_shouldReturn405ForGetRequest() throws Exception {

        mockMvc.perform(get("/api/v1/auth/login"))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(authService);
    }

    @Test
    void logout_shouldReturn204WhenSuccessful() throws Exception {

        doNothing().when(authService).logout(any(), any());

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(authService).logout(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        );
    }

    // 2. Logout should propagate service exceptions to exception handling.
    // Logout service failure should propagate as ServletException
    @Test
    void logout_shouldPropagateExceptionWhenServiceFails() {

        doThrow(new RuntimeException("Logout failed"))
                .when(authService).logout(any(), any());

        org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(post("/api/v1/auth/logout"))
        );

        verify(authService).logout(any(), any());
    }

    // 3. Refresh should return 200 when successful.
    @Test
    void refreshToken_shouldReturn200WhenSuccessful() throws Exception {

        TokenResponse response = mock(TokenResponse.class);

        when(authService.refreshToken(
                any(),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isOk());

        verify(authService).refreshToken(
                any(),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        );
    }

    // 4. Refresh should handle an invalid refresh token.
    @Test
    void refreshToken_shouldHandleInvalidToken() throws Exception {

        when(authService.refreshToken(
                any(),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        )).thenThrow(new org.springframework.security.authentication.BadCredentialsException(
                "Invalid refresh token"
        ));

        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());

        verify(authService).refreshToken(any(), any(), any());
    }

    // 5. Email verification should return the service response.
    @Test
    void verifyEmail_shouldReturn200WhenSuccessful() throws Exception {

        when(authService.verifyEmailAndSendOtp("test@gmail.com"))
                .thenReturn(ResponseEntity.ok("OTP sent successfully"));

        mockMvc.perform(post("/api/v1/auth/verifyEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@gmail.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent successfully"));

        verify(authService).verifyEmailAndSendOtp("test@gmail.com");
    }

    // 6. OTP verification should return the service response.
    @Test
    void verifyOtp_shouldReturn200WhenOtpIsValid() throws Exception {

        when(authService.verifyOtp("test@gmail.com", "123456"))
                .thenReturn(ResponseEntity.ok("OTP verified"));

        mockMvc.perform(post("/api/v1/auth/verifyOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@gmail.com",
                                    "otp": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP verified"));

        verify(authService).verifyOtp("test@gmail.com", "123456");
    }

    // 7. Password reset should return the service response.
    @Test
    void resetPassword_shouldReturn200WhenSuccessful() throws Exception {

        when(authService.resetPassword(
                "test@gmail.com",
                "newPassword123",
                "newPassword123"
        )).thenReturn(ResponseEntity.ok("Password reset successfully"));

        mockMvc.perform(post("/api/v1/auth/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@gmail.com",
                                    "rewrittenPassword": "newPassword123",
                                    "password": "newPassword123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successfully"));

        verify(authService).resetPassword(
                "test@gmail.com",
                "newPassword123",
                "newPassword123"
        );
    }
}