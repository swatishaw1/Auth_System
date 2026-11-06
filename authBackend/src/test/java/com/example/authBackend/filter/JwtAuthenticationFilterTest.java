package com.example.authBackend.filter;

import com.example.authBackend.Enum.Role;
import com.example.authBackend.model.User;
import com.example.authBackend.repository.UserRepository;
import com.example.authBackend.utils.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private User user;

    private UUID userId;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userRepository);
        userId = UUID.randomUUID();
    }

    @AfterEach
    void cleanUp() {

        // Cleanup: Clear SecurityContext after every test.
        SecurityContextHolder.clearContext();
    }

    // 1. Request without Authorization header.
    @Test
    void doFilterInternal_shouldContinueWhenAuthorizationHeaderIsMissing()
            throws Exception {

        // Arrange: Create a request without an Authorization header.
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act: Execute the JWT filter.
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Verify: Request continues through the filter chain.
        verify(filterChain).doFilter(request, response);

        // Verify: No JWT validation or database lookup occurs.
        verifyNoInteractions(jwtService, userRepository);
    }

    // 2. Valid access token authenticates the user.
    @Test
    void doFilterInternal_shouldAuthenticateUserWithValidAccessToken()
            throws Exception {

        // Arrange: Create a request with a valid Bearer token.
        String token = "valid-access-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isAccessToken(token)).thenReturn(true);

        @SuppressWarnings("unchecked")
        Jws<Claims> parsedJwt = mock(Jws.class);

        Claims claims = mock(Claims.class);

        when(jwtService.parse(token)).thenReturn(parsedJwt);
        when(parsedJwt.getPayload()).thenReturn(claims);
        when(claims.getSubject()).thenReturn(userId.toString());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Configure mocked user details BEFORE executing the filter.
        when(user.isEnabled()).thenReturn(true);
        when(user.getEmail()).thenReturn("test@gmail.com");
        when(user.getRole()).thenReturn(Role.USER);

        // Act: Execute the JWT filter.
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert: Verify authentication.
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals("test@gmail.com", authentication.getPrincipal());
        assertTrue(authentication.isAuthenticated());

        // Verify: Request continues through the filter chain.
        verify(filterChain).doFilter(request, response);

        // Verify: User was retrieved using the JWT subject.
        verify(userRepository).findById(userId);
    }

    // 3. Reject a refresh token when it is used as an access token.
    @Test
    void doFilterInternal_shouldNotAuthenticateRefreshToken()
            throws Exception {

        // Arrange: Create a request containing a refresh token.
        String token = "refresh-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isAccessToken(token)).thenReturn(false);

        // Act: Execute the JWT filter.
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert: Verify that no authentication was created.
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify: Request continues without authentication.
        verify(filterChain).doFilter(request, response);

        // Verify: Token parsing and database lookup are skipped.
        verify(jwtService, never()).parse(anyString());
        verifyNoInteractions(userRepository);
    }

    // 4. Invalid JWT must not authenticate the user.
    @Test
    void doFilterInternal_shouldRejectInvalidToken()
            throws Exception {

        // Arrange: Create a request with a malformed token.
        String token = "invalid-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isAccessToken(token))
                .thenThrow(new JwtException("Invalid JWT"));

        // Act: Execute the JWT filter.
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert: Verify that the authentication context remains empty.
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        assertEquals("Invalid token", request.getAttribute("error"));

        // Verify: Request continues through the filter chain.
        verify(filterChain).doFilter(request, response);

        // Verify: User lookup does not happen.
        verifyNoInteractions(userRepository);
    }

    // 5. Do not authenticate when the user does not exist.
    @Test
    void doFilterInternal_shouldNotAuthenticateWhenUserDoesNotExist()
            throws Exception {

        // Arrange: Create a valid token belonging to an unknown user.
        String token = "valid-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isAccessToken(token)).thenReturn(true);

        @SuppressWarnings("unchecked")
        Jws<Claims> parsedJwt = mock(Jws.class);

        Claims claims = mock(Claims.class);

        when(jwtService.parse(token)).thenReturn(parsedJwt);
        when(parsedJwt.getPayload()).thenReturn(claims);
        when(claims.getSubject()).thenReturn(userId.toString());

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act: Execute the JWT filter.
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert: User must remain unauthenticated.
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify: Request continues through the filter chain.
        verify(filterChain).doFilter(request, response);
    }

    // 6. Do not authenticate a disabled user.
    @Test
    void doFilterInternal_shouldNotAuthenticateDisabledUser()
            throws Exception {

        // Arrange: Create a valid token for a disabled user.
        String token = "valid-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isAccessToken(token)).thenReturn(true);

        @SuppressWarnings("unchecked")
        Jws<Claims> parsedJwt = mock(Jws.class);

        Claims claims = mock(Claims.class);

        when(jwtService.parse(token)).thenReturn(parsedJwt);
        when(parsedJwt.getPayload()).thenReturn(claims);
        when(claims.getSubject()).thenReturn(userId.toString());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(user.isEnabled()).thenReturn(false);

        // Act: Execute the JWT filter.
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert: Disabled user must not be authenticated.
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify: Request continues through the filter chain.
        verify(filterChain).doFilter(request, response);
    }

    // 7. Do not overwrite an existing authentication.
    @Test
    void doFilterInternal_shouldNotOverwriteExistingAuthentication()
            throws Exception {

        // Arrange: Set an existing authentication in SecurityContext.
        Authentication existingAuthentication = mock(Authentication.class);

        SecurityContextHolder.getContext()
                .setAuthentication(existingAuthentication);

        String token = "valid-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isAccessToken(token)).thenReturn(true);

        @SuppressWarnings("unchecked")
        Jws<Claims> parsedJwt = mock(Jws.class);

        Claims claims = mock(Claims.class);

        when(jwtService.parse(token)).thenReturn(parsedJwt);
        when(parsedJwt.getPayload()).thenReturn(claims);
        when(claims.getSubject()).thenReturn(userId.toString());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(user.isEnabled()).thenReturn(true);

        // Act: Execute the JWT filter.
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert: Original authentication must remain unchanged.
        assertSame(
                existingAuthentication,
                SecurityContextHolder.getContext().getAuthentication()
        );

        // Verify: Request continues through the filter chain.
        verify(filterChain).doFilter(request, response);
    }

    // 8. Skip JWT processing for authentication endpoints.
    @Test
    void doFilterInternal_shouldSkipAuthenticationEndpoints()
            throws Exception {

        // Arrange: Create a request targeting the authentication endpoint.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/auth/login");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act: Execute the JWT filter.
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Verify: The filter chain continues, but JWT services are not called.
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);
    }
}
