package com.example.authBackend.security;

import com.example.authBackend.Enum.Role;
import com.example.authBackend.controller.AuthController;
import com.example.authBackend.model.User;
import com.example.authBackend.repository.UserRepository;
import com.example.authBackend.security.oauth2.OAuth2SuccessHandler;
import com.example.authBackend.security.oauth2.Oauth2FailureHandler;
import com.example.authBackend.service.EmailService;
import com.example.authBackend.service.implementation.AuthServiceImpl;
import com.example.authBackend.utils.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "app.cors.frontend-url=http://localhost:3000"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(SecurityIntegrationTest.ProtectedTestController.class)
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private Oauth2FailureHandler oauth2FailureHandler;

    @MockitoBean
    private AuthController authController;

    @MockitoBean
    private AuthServiceImpl authService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    private UUID userId;

    private User user;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@gmail.com")
                .role(Role.USER)
                .enable(true)
                .build();
    }

    // 1. Request without JWT should be rejected.
    @Test
    void security_shouldRejectRequestWithoutToken() throws Exception {

        mockMvc.perform(get("/api/v1/security-test/protected"))
                .andExpect(status().isUnauthorized());

        // JWT validation and user lookup should not happen.
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userRepository);
    }

    // 2. Invalid JWT should be rejected.
    @Test
    void security_shouldRejectInvalidToken() throws Exception {

        String token = "invalid-token";

        when(jwtService.isAccessToken(token))
                .thenThrow(new JwtException("Invalid token"));

        mockMvc.perform(get("/api/v1/security-test/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        verify(jwtService).isAccessToken(token);
        verify(userRepository, never()).findById(any());
    }

    // 3. Expired JWT should be rejected.
    @Test
    void security_shouldRejectExpiredToken() throws Exception {

        String token = "expired-token";

        when(jwtService.isAccessToken(token))
                .thenThrow(new ExpiredJwtException(
                        null, null, "Token expired"
                ));

        mockMvc.perform(get("/api/v1/security-test/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        verify(jwtService).isAccessToken(token);
        verify(userRepository, never()).findById(any());
    }

    // 4. Refresh token must not authenticate protected endpoints.
    @Test
    void security_shouldRejectRefreshTokenAsAccessToken() throws Exception {

        String token = "refresh-token";

        when(jwtService.isAccessToken(token))
                .thenReturn(false);

        mockMvc.perform(get("/api/v1/security-test/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        verify(jwtService).isAccessToken(token);
        verify(jwtService, never()).parse(anyString());
        verifyNoInteractions(userRepository);
    }

    // 5. Valid access token should authenticate the user.
    @Test
    void security_shouldAllowRequestWithValidAccessToken() throws Exception {

        String token = "valid-access-token";

        when(jwtService.isAccessToken(token))
                .thenReturn(true);

        @SuppressWarnings("unchecked")
        Jws<Claims> parsedJwt = mock(Jws.class);

        Claims claims = mock(Claims.class);

        when(jwtService.parse(token)).thenReturn(parsedJwt);
        when(parsedJwt.getPayload()).thenReturn(claims);
        when(claims.getSubject()).thenReturn(userId.toString());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/security-test/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Authenticated: test@gmail.com"));

        verify(jwtService).isAccessToken(token);
        verify(jwtService).parse(token);
        verify(userRepository).findById(userId);
    }

    // 6. Disabled users must not access protected endpoints.
    @Test
    void security_shouldRejectDisabledUser() throws Exception {

        String token = "valid-token";

        user = User.builder()
                .id(userId)
                .name("Disabled User")
                .email("disabled@gmail.com")
                .role(Role.USER)
                .enable(false)
                .build();

        when(jwtService.isAccessToken(token))
                .thenReturn(true);

        @SuppressWarnings("unchecked")
        Jws<Claims> parsedJwt = mock(Jws.class);

        Claims claims = mock(Claims.class);

        when(jwtService.parse(token)).thenReturn(parsedJwt);
        when(parsedJwt.getPayload()).thenReturn(claims);
        when(claims.getSubject()).thenReturn(userId.toString());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/security-test/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        verify(userRepository).findById(userId);
    }

    // Test-only protected endpoint.
    @RestController
    static class ProtectedTestController {

        @GetMapping("/api/v1/security-test/protected")
        public String protectedEndpoint(Authentication authentication) {

            return "Authenticated: " + authentication.getName();
        }
    }
}
