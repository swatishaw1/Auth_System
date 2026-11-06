package com.example.authBackend.service;

import com.example.authBackend.model.RefreshToken;
import com.example.authBackend.model.User;
import com.example.authBackend.repository.RefreshTokenRepository;
import com.example.authBackend.service.implementation.RefreshTokenServiceImpl;
import com.example.authBackend.utils.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    private RefreshTokenServiceImpl refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {

        // Arrange: Initialize the service with mocked dependencies.
        refreshTokenService = new RefreshTokenServiceImpl(
                refreshTokenRepository,
                jwtService
        );

        // Arrange: Create a sample user for refresh-token operations.
        user = mock(User.class);
    }

    // 1. Create a new refresh token.
    @Test
    void createOrUpdate_shouldCreateNewRefreshToken() {

        // Arrange: No refresh token currently exists for this user.
        when(refreshTokenRepository.getRefreshTokenByUser(user))
                .thenReturn(null);

        when(jwtService.getRefreshTokenValiditySeconds())
                .thenReturn(604800L);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Create a refresh token using a new JTI.
        RefreshToken result = refreshTokenService.createOrUpdate(
                user,
                "new-jti"
        );

        // Assert: Verify that the token is created with correct details.
        assertNotNull(result);
        assertEquals("new-jti", result.getJti());
        assertEquals(user, result.getUser());
        assertFalse(result.isRevoked());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getExpiresAt());

        // Verify: Expiry is approximately seven days from creation.
        assertTrue(
                result.getExpiresAt().isAfter(Instant.now().plusSeconds(603000))
        );

        // Verify: Ensure the token is saved.
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    // 2. Update an existing refresh token instead of creating another record.
    @Test
    void createOrUpdate_shouldUpdateExistingRefreshToken() {

        // Arrange: Create an existing refresh-token record.
        RefreshToken existingToken = RefreshToken.builder()
                .jti("old-jti")
                .user(user)
                .revoked(true)
                .build();

        when(refreshTokenRepository.getRefreshTokenByUser(user))
                .thenReturn(existingToken);

        when(jwtService.getRefreshTokenValiditySeconds())
                .thenReturn(604800L);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Update the existing refresh token.
        RefreshToken result = refreshTokenService.createOrUpdate(
                user,
                "new-jti"
        );

        // Assert: Verify that the existing record is updated.
        assertSame(existingToken, result);
        assertEquals("new-jti", result.getJti());
        assertEquals(user, result.getUser());
        assertFalse(result.isRevoked());

        // Verify: The existing record is saved, not replaced with another object.
        verify(refreshTokenRepository).save(existingToken);
    }

    // 3. Validate a valid refresh token.
    @Test
    void validate_shouldReturnTokenWhenValid() {

        // Arrange: Create a valid stored refresh token.
        RefreshToken storedToken = RefreshToken.builder()
                .jti("valid-jti")
                .user(user)
                .revoked(false)
                .expiresAt(Instant.now().plusSeconds(600))
                .build();

        when(jwtService.isRefreshToken("valid-token"))
                .thenReturn(true);

        when(jwtService.getJwtId("valid-token"))
                .thenReturn("valid-jti");

        when(refreshTokenRepository.findByJti("valid-jti"))
                .thenReturn(Optional.of(storedToken));

        // Act: Validate the refresh token.
        RefreshToken result = refreshTokenService.validate("valid-token");

        // Assert: Verify that the stored token is returned.
        assertNotNull(result);
        assertSame(storedToken, result);

        // Verify: Ensure the correct JTI was searched.
        verify(refreshTokenRepository).findByJti("valid-jti");
    }

    // 4. Reject an access token used as a refresh token.
    @Test
    void validate_shouldRejectAccessToken() {

        // Arrange: Simulate a token that is not a refresh token.
        when(jwtService.isRefreshToken("access-token"))
                .thenReturn(false);

        // Act and Assert: An access token must be rejected.
        assertThrows(
                BadCredentialsException.class,
                () -> refreshTokenService.validate("access-token")
        );

        // Verify: Repository lookup must not happen.
        verifyNoInteractions(refreshTokenRepository);
    }

    // 5. Reject a refresh token whose JTI is not stored.
    @Test
    void validate_shouldRejectUnknownRefreshToken() {

        // Arrange: Token is valid in format but not present in the database.
        when(jwtService.isRefreshToken("unknown-token"))
                .thenReturn(true);

        when(jwtService.getJwtId("unknown-token"))
                .thenReturn("unknown-jti");

        when(refreshTokenRepository.findByJti("unknown-jti"))
                .thenReturn(Optional.empty());

        // Act and Assert: Unknown refresh token must be rejected.
        assertThrows(
                BadCredentialsException.class,
                () -> refreshTokenService.validate("unknown-token")
        );
    }

    // 6. Reject a refresh token that has already been revoked.
    @Test
    void validate_shouldRejectRevokedToken() {

        // Arrange: Create a stored token marked as revoked.
        RefreshToken revokedToken = RefreshToken.builder()
                .jti("revoked-jti")
                .user(user)
                .revoked(true)
                .expiresAt(Instant.now().plusSeconds(600))
                .build();

        when(jwtService.isRefreshToken("revoked-token"))
                .thenReturn(true);

        when(jwtService.getJwtId("revoked-token"))
                .thenReturn("revoked-jti");

        when(refreshTokenRepository.findByJti("revoked-jti"))
                .thenReturn(Optional.of(revokedToken));

        // Act and Assert: Revoked token must be rejected.
        assertThrows(
                BadCredentialsException.class,
                () -> refreshTokenService.validate("revoked-token")
        );
    }

    // 7. Reject a refresh token whose stored expiry time has passed.
    @Test
    void validate_shouldRejectExpiredToken() {

        // Arrange: Create a stored token with an expiry time in the past.
        RefreshToken expiredToken = RefreshToken.builder()
                .jti("expired-jti")
                .user(user)
                .revoked(false)
                .expiresAt(Instant.now().minusSeconds(60))
                .build();

        when(jwtService.isRefreshToken("expired-token"))
                .thenReturn(true);

        when(jwtService.getJwtId("expired-token"))
                .thenReturn("expired-jti");

        when(refreshTokenRepository.findByJti("expired-jti"))
                .thenReturn(Optional.of(expiredToken));

        // Act and Assert: Expired token must be rejected.
        assertThrows(
                BadCredentialsException.class,
                () -> refreshTokenService.validate("expired-token")
        );
    }

    // 8. Rotate an existing refresh token with a new JTI and expiry.
    @Test
    void rotate_shouldUpdateRefreshTokenSuccessfully() {

        // Arrange: Create an existing refresh-token record.
        RefreshToken oldToken = RefreshToken.builder()
                .jti("old-jti")
                .user(user)
                .revoked(false)
                .build();

        when(jwtService.getRefreshTokenValiditySeconds())
                .thenReturn(604800L);

        when(refreshTokenRepository.save(oldToken))
                .thenReturn(oldToken);

        // Act: Rotate the existing token.
        RefreshToken result = refreshTokenService.rotate(
                oldToken,
                "new-jti",
                "new-refresh-token"
        );

        // Assert: Verify that the token has been updated.
        assertSame(oldToken, result);
        assertEquals("new-jti", result.getJti());
        assertEquals("new-refresh-token", result.getReplacedByToken());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getExpiresAt());

        // Verify: Ensure the rotated token is saved.
        verify(refreshTokenRepository).save(oldToken);
    }

    // 9. Revoke an existing refresh token.
    @Test
    void revoke_shouldMarkTokenAsRevoked() {

        // Arrange: Create an active refresh-token record.
        RefreshToken token = RefreshToken.builder()
                .jti("active-jti")
                .user(user)
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByJti("active-jti"))
                .thenReturn(Optional.of(token));

        when(refreshTokenRepository.save(token))
                .thenReturn(token);

        // Act: Revoke the token using its JTI.
        refreshTokenService.revoke("active-jti");

        // Assert: Verify that the token is marked as revoked.
        assertTrue(token.isRevoked());

        // Verify: Ensure the updated token is saved.
        verify(refreshTokenRepository).save(token);
    }

    // 10. Do nothing when trying to revoke a nonexistent token.
    @Test
    void revoke_shouldDoNothingWhenTokenDoesNotExist() {

        // Arrange: Simulate a JTI that is not present in the database.
        when(refreshTokenRepository.findByJti("unknown-jti"))
                .thenReturn(Optional.empty());

        // Act: Attempt to revoke the nonexistent token.
        assertDoesNotThrow(() ->
                refreshTokenService.revoke("unknown-jti")
        );

        // Verify: No token should be saved.
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }
}
