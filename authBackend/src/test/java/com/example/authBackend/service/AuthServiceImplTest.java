package com.example.authBackend.service;

import com.example.authBackend.Enum.Provider;
import com.example.authBackend.Enum.Role;
import com.example.authBackend.api.request.LoginRequest;
import com.example.authBackend.api.request.RefreshTokenRequest;
import com.example.authBackend.api.response.TokenResponse;
import com.example.authBackend.dto.UserDTO;
import com.example.authBackend.model.ForgetPassword;
import com.example.authBackend.model.RefreshToken;
import com.example.authBackend.model.User;
import com.example.authBackend.repository.ForgetPasswordRepository;
import com.example.authBackend.repository.UserRepository;
import com.example.authBackend.security.CookieService;
import com.example.authBackend.service.*;
import com.example.authBackend.service.implementation.AuthServiceImpl;
import com.example.authBackend.utils.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtService jwtService;

    @Mock
    private CookieService cookieService;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ForgetPasswordRepository forgetPasswordRepository;
    @Mock
    private HttpServletResponse response;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {

        authService = new AuthServiceImpl(
                userRepository,
                modelMapper,
                userService,
                authenticationService,
                refreshTokenService,
                jwtService,
                cookieService,
                emailService,
                passwordEncoder,
                forgetPasswordRepository
        );
    }

    private User createUser(Provider provider, boolean enabled) {

        return User.builder()
                .name("Swati")
                .email("swati@example.com")
                .password("encodedPassword")
                .provider(provider)
                .role(Role.USER)
                .enable(enabled)
                .build();
    }

    // 1. Successful login
    @Test
    void loginUser_shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest("swati@example.com", "password123");

        User user = createUser(Provider.LOCAL, true);

        UserDTO userDTO = UserDTO.builder()
                .name("Swati")
                .email("swati@example.com")
                .build();

        when(userRepository.findByEmail("swati@example.com")).thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(user)).thenReturn("access-token");

        when(jwtService.generateRefreshedToken(eq(user), anyString())).thenReturn("refresh-token");

        when(jwtService.getAccessTokenValiditySeconds()).thenReturn(900L);

        when(jwtService.getRefreshTokenValiditySeconds()).thenReturn(604800L);

        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        TokenResponse result = authService.loginUser(request, response);

        assertNotNull(result);

        verify(userRepository).findByEmail("swati@example.com");
        verify(authenticationService).authenticate("swati@example.com", "password123");
        verify(jwtService).generateAccessToken(user);
        verify(jwtService).generateRefreshedToken(eq(user), anyString());
        verify(refreshTokenService).createOrUpdate(eq(user), anyString());
        verify(cookieService).attachRefreshCookie(response, "refresh-token", 604800);
        verify(cookieService).attachAccessTokenCookie(response, "access-token", 900);
        verify(cookieService).addNoStoreHeaders(response);
    }

    // 2. User does not exist
    @Test
    void loginUser_shouldThrowExceptionWhenUserDoesNotExist() {

        LoginRequest request =
                new LoginRequest("unknown@example.com", "password123");

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                BadCredentialsException.class,
                () -> authService.loginUser(request, response)
        );

        verifyNoInteractions(authenticationService);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenService);
    }

    // 3. Google user tries email/password login
    @Test
    void loginUser_shouldRejectGoogleUser() {

        LoginRequest request =
                new LoginRequest("google@example.com", "password123");

        User user = createUser(Provider.GOOGLE, true);

        when(userRepository.findByEmail("google@example.com"))
                .thenReturn(Optional.of(user));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.loginUser(request, response)
        );

        verifyNoInteractions(authenticationService);
        verifyNoInteractions(jwtService);
    }

    // 4. GitHub user tries email/password login
    @Test
    void loginUser_shouldRejectGithubUser() {

        LoginRequest request =
                new LoginRequest("github@example.com", "password123");

        User user = createUser(Provider.GITHUB, true);

        when(userRepository.findByEmail("github@example.com")).thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class,
                () -> authService.loginUser(request, response)
        );

        verifyNoInteractions(authenticationService);
        verifyNoInteractions(jwtService);
    }

    // 5. Incorrect password / authentication failure
    @Test
    void loginUser_shouldRejectInvalidCredentials() {

        LoginRequest request =
                new LoginRequest("swati@example.com", "wrongPassword");

        User user = createUser(Provider.LOCAL, true);

        when(userRepository.findByEmail("swati@example.com"))
                .thenReturn(Optional.of(user));

        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationService)
                .authenticate("swati@example.com", "wrongPassword");

        assertThrows(
                BadCredentialsException.class,
                () -> authService.loginUser(request, response)
        );

        verify(authenticationService)
                .authenticate("swati@example.com", "wrongPassword");

        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenService);
    }

    // 6. Disabled user
    @Test
    void loginUser_shouldRejectDisabledUser() {

        LoginRequest request =
                new LoginRequest("swati@example.com", "password123");

        User user = createUser(Provider.LOCAL, false);

        when(userRepository.findByEmail("swati@example.com"))
                .thenReturn(Optional.of(user));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.loginUser(request, response)
        );

        verify(authenticationService)
                .authenticate("swati@example.com", "password123");

        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void refreshToken_shouldGenerateNewTokensSuccessfully() {

        // Arrange: Create a valid stored refresh token and associate it with a user.
        User user = createUser(Provider.LOCAL, true);

        RefreshToken storedToken = RefreshToken.builder()
                .jti("old-jti")
                .user(user)
                .revoked(false)
                .build();

        HttpServletRequest request = mock(HttpServletRequest.class);

        RefreshTokenRequest body = new RefreshTokenRequest("old-refresh-token");

        when(refreshTokenService.validate("old-refresh-token"))
                .thenReturn(storedToken);

        when(jwtService.generateAccessToken(user))
                .thenReturn("new-access-token");

        when(jwtService.generateRefreshedToken(eq(user), anyString()))
                .thenReturn("new-refresh-token");

        when(jwtService.getAccessTokenValiditySeconds()).thenReturn(900L);
        when(jwtService.getRefreshTokenValiditySeconds()).thenReturn(604800L);

        when(modelMapper.map(user, UserDTO.class))
                .thenReturn(UserDTO.builder().email(user.getEmail()).build());

        // Act: Refresh the tokens using the existing refresh token.
        TokenResponse result = authService.refreshToken(body, request, response);

        // Assert: Verify that a new token response is returned.
        assertNotNull(result);

        // Verify: Ensure the old token is validated and rotated.
        verify(refreshTokenService).validate("old-refresh-token");
        verify(refreshTokenService).rotate(
                eq(storedToken),
                anyString(),
                eq("new-refresh-token")
        );

        // Verify: Ensure new authentication cookies are attached.
        verify(cookieService).attachRefreshCookie(
                response, "new-refresh-token", 604800
        );

        verify(cookieService).attachAccessTokenCookie(
                response, "new-access-token", 900
        );

        verify(cookieService).addNoStoreHeaders(response);
    }


    @Test
    void refreshToken_shouldRejectMissingRefreshToken() {

        // Arrange: Create a request without cookies, body token or refresh headers.
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("X-Refresh-Token")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act and Assert: Missing refresh token must reject the request.
        assertThrows(
                BadCredentialsException.class,
                () -> authService.refreshToken(null, request, response)
        );

        // Verify: Token validation must not be attempted.
        verifyNoInteractions(refreshTokenService);
        verifyNoInteractions(jwtService);
    }


    @Test
    void refreshToken_shouldRejectInvalidRefreshToken() {

        // Arrange: Provide a token that fails refresh-token validation.
        HttpServletRequest request = mock(HttpServletRequest.class);

        RefreshTokenRequest body = new RefreshTokenRequest("invalid-token");

        when(request.getCookies()).thenReturn(null);

        when(refreshTokenService.validate("invalid-token"))
                .thenThrow(new BadCredentialsException("Invalid refresh token"));

        // Act and Assert: Invalid refresh token must be rejected.
        assertThrows(
                BadCredentialsException.class,
                () -> authService.refreshToken(body, request, response)
        );

        // Verify: No new tokens should be generated.
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void refreshToken_shouldRejectExpiredRefreshToken() {

        // Arrange: Simulate an expired token during validation.
        HttpServletRequest request = mock(HttpServletRequest.class);

        RefreshTokenRequest body = new RefreshTokenRequest("expired-token");

        when(request.getCookies()).thenReturn(null);

        when(refreshTokenService.validate("expired-token"))
                .thenThrow(new BadCredentialsException("Refresh token expired"));

        // Act and Assert: Expired refresh token must be rejected.
        assertThrows(
                BadCredentialsException.class,
                () -> authService.refreshToken(body, request, response)
        );

        // Verify: Token generation must not happen.
        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).rotate(any(), anyString(), anyString());
    }


    @Test
    void logout_shouldClearCookiesAndSecurityContext() {

        // Arrange: Create a request without a refresh token.
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(anyString())).thenReturn(null);

        // Act: Logout the user.
        authService.logout(request, response);

        // Verify: Both authentication cookies are cleared.
        verify(cookieService).clearRefreshCookie(response);
        verify(cookieService).clearAccessCookie(response);

        // Verify: No-store headers are added.
        verify(cookieService).addNoStoreHeaders(response);
    }

    @Test
    void logout_shouldRevokeValidRefreshToken() {

        // Arrange: Provide a valid refresh token through a cookie.
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(cookieService.getRefreshTokenCookieName())
                .thenReturn("refreshToken");

        when(request.getCookies())
                .thenReturn(new Cookie[]{
                        new Cookie("refreshToken", "valid-refresh-token")
                });

        when(jwtService.isRefreshToken("valid-refresh-token"))
                .thenReturn(true);

        when(jwtService.getJwtId("valid-refresh-token"))
                .thenReturn("token-jti");

        // Act: Logout the user.
        authService.logout(request, response);

        // Verify: The refresh token is revoked using its JWT ID.
        verify(refreshTokenService).revoke("token-jti");

        // Verify: Authentication cookies are cleared.
        verify(cookieService).clearRefreshCookie(response);
        verify(cookieService).clearAccessCookie(response);
    }

    @Test
    void logout_shouldHandleInvalidRefreshToken() {

        // Arrange: Provide a malformed refresh token.
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(cookieService.getRefreshTokenCookieName())
                .thenReturn("refreshToken");

        when(request.getCookies())
                .thenReturn(new Cookie[]{
                        new Cookie("refreshToken", "invalid-token")
                });

        when(jwtService.isRefreshToken("invalid-token"))
                .thenThrow(new JwtException("Invalid token"));

        // Act: Logout should complete without propagating the JWT exception.
        assertDoesNotThrow(() -> authService.logout(request, response));

        // Verify: Invalid tokens are not revoked.
        verify(refreshTokenService, never()).revoke(anyString());

        // Verify: Cookies are still cleared.
        verify(cookieService).clearRefreshCookie(response);
        verify(cookieService).clearAccessCookie(response);
    }


    @Test
    void verifyEmailAndSendOtp_shouldSendOtpSuccessfully() {

        // Arrange: Mock an existing user and configure OTP expiry.
        User user = createUser(Provider.LOCAL, true);

        org.springframework.test.util.ReflectionTestUtils.setField(
                authService, "otpValidationTime", 300000
        );

        when(emailService.verifyEmail("swati@example.com"))
                .thenReturn(user);

        // Act: Request email verification.
        var result = authService.verifyEmailAndSendOtp("swati@example.com");

        // Assert: Verify successful response.
        assertEquals(200, result.getStatusCode().value());

        // Verify: OTP is emailed and saved for verification.
        verify(emailService).sendOtpEmail(eq("swati@example.com"), anyLong());
        verify(forgetPasswordRepository).save(any());
    }

    @Test
    void verifyEmailAndSendOtp_shouldRejectUnknownEmail() {

        // Arrange: Simulate an email that does not belong to any user.
        when(emailService.verifyEmail("unknown@example.com"))
                .thenReturn(null);

        // Act: Request OTP for an unknown email.
        var result = authService.verifyEmailAndSendOtp("unknown@example.com");

        // Assert: Verify that the request is rejected.
        assertEquals(400, result.getStatusCode().value());

        // Verify: No OTP email or database record should be created.
        verify(emailService, never()).sendOtpEmail(anyString(), anyLong());
        verify(forgetPasswordRepository, never()).save(any());
    }

    @Test
    void verifyOtp_shouldVerifyCorrectOtp() {

        // Arrange: Create a user and an OTP that has not expired.
        User user = createUser(Provider.LOCAL, true);
        ForgetPassword forgetPassword = mock(ForgetPassword.class);

        when(userRepository.findByEmail("swati@example.com"))
                .thenReturn(Optional.of(user));

        when(forgetPasswordRepository.findByOtpAndUser(123456L, user))
                .thenReturn(Optional.of(forgetPassword));

        when(forgetPassword.getExpiryDate())
                .thenReturn(new Date(System.currentTimeMillis() + 60000));

        // Act: Verify the correct OTP.
        var result = authService.verifyOtp("swati@example.com", "123456");

        // Assert: Verify successful response.
        assertEquals(200, result.getStatusCode().value());

        // Verify: OTP is marked as verified and saved.
        verify(forgetPassword).setVerified(true);
        verify(forgetPasswordRepository).save(forgetPassword);
    }

    @Test
    void verifyOtp_shouldRejectIncorrectOtp() {

        // Arrange: Create a user but make the OTP lookup return empty.
        User user = createUser(Provider.LOCAL, true);

        when(userRepository.findByEmail("swati@example.com"))
                .thenReturn(Optional.of(user));

        when(forgetPasswordRepository.findByOtpAndUser(111111L, user))
                .thenReturn(Optional.empty());

        // Act and Assert: Incorrect OTP must be rejected.
        assertThrows(
                RuntimeException.class,
                () -> authService.verifyOtp("swati@example.com", "111111")
        );
    }


    @Test
    void verifyOtp_shouldRejectExpiredOtp() {

        // Arrange: Create an OTP whose expiry date is in the past.
        User user = createUser(Provider.LOCAL, true);
        ForgetPassword forgetPassword = mock(ForgetPassword.class);

        when(userRepository.findByEmail("swati@example.com"))
                .thenReturn(Optional.of(user));

        when(forgetPasswordRepository.findByOtpAndUser(123456L, user))
                .thenReturn(Optional.of(forgetPassword));

        when(forgetPassword.getExpiryDate())
                .thenReturn(new Date(System.currentTimeMillis() - 60000));


        when(forgetPassword.getId()).thenReturn(1L);

        // Act: Verify the expired OTP.
        var result = authService.verifyOtp("swati@example.com", "123456");

        // Assert: Verify that the service returns an expiry response.
        assertEquals(417, result.getStatusCode().value());

        // Verify: Expired OTP is deleted.
        verify(forgetPasswordRepository).deleteById(forgetPassword.getId());
    }

    @Test
    void resetPassword_shouldUpdatePasswordSuccessfully() {

        // Arrange: Create a user with a verified OTP.
        User user = createUser(Provider.LOCAL, true);
        ForgetPassword forgetPassword = mock(ForgetPassword.class);

        when(userRepository.findByEmail("swati@example.com"))
                .thenReturn(Optional.of(user));

        when(forgetPasswordRepository.findByUserAndVerifiedTrue(user))
                .thenReturn(Optional.of(forgetPassword));

        when(passwordEncoder.encode("newPassword123"))
                .thenReturn("encodedNewPassword");

        // Act: Reset the user's password.
        var result = authService.resetPassword(
                "swati@example.com",
                "newPassword123",
                "newPassword123"
        );

        // Assert: Verify successful response.
        assertEquals(200, result.getStatusCode().value());

        // Verify: Password is encoded before saving.
        assertEquals("encodedNewPassword", user.getPassword());
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(user);

        // Verify: Used OTP verification record is deleted.
        verify(forgetPasswordRepository).delete(forgetPassword);
    }

    @Test
    void resetPassword_shouldRejectPasswordMismatch() {

        // Arrange: Provide two different passwords.
        String password = "newPassword123";
        String rewrittenPassword = "differentPassword";

        // Act: Attempt password reset.
        var result = authService.resetPassword(
                "swati@example.com",
                rewrittenPassword,
                password
        );

        // Assert: Verify bad request response.
        assertEquals(400, result.getStatusCode().value());

        // Verify: Database and password encoder should not be accessed.
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void resetPassword_shouldRejectWhenOtpIsNotVerified() {

        // Arrange: Create a user without a verified OTP record.
        User user = createUser(Provider.LOCAL, true);

        when(userRepository.findByEmail("swati@example.com"))
                .thenReturn(Optional.of(user));

        when(forgetPasswordRepository.findByUserAndVerifiedTrue(user))
                .thenReturn(Optional.empty());

        // Act and Assert: Password reset must be rejected.
        assertThrows(
                BadCredentialsException.class,
                () -> authService.resetPassword(
                        "swati@example.com",
                        "newPassword123",
                        "newPassword123"
                )
        );

        // Verify: Password must not be encoded or updated.
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }
}