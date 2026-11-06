
package com.example.authBackend.integration;

import com.example.authBackend.*;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(RateLimiterIntegrationTest.ProbeConfig.class)
class RateLimiterIntegrationTest {

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    @Autowired
    private RateLimiterProbe probe;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    void resetLimiters() {

        rateLimiterRegistry.getAllRateLimiters()
                .forEach(limiter ->
                        limiter.changeLimitForPeriod(limiter.getRateLimiterConfig().getLimitForPeriod())
                );
    }

    // Common method to test rate limits
    private void assertRateLimit(
            int allowedRequests,
            Runnable operation
    ) {

        // Requests within the limit should be accepted
        for (int i = 0; i < allowedRequests; i++) {
            assertDoesNotThrow(operation::run);
        }

        // The next request should be rejected
        assertThrows(
                RequestNotPermitted.class,
                operation::run
        );
    }

    // 1. Login: 5 requests per minute
    @Test
    void loginShouldRejectSixthRequest() {
        assertRateLimit(5, probe::login);
    }

    // 2. Register: 3 requests per hour
    @Test
    void registerShouldRejectFourthRequest() {
        assertRateLimit(3, probe::register);
    }

    // 3. Send OTP: 3 requests per 10 minutes
    @Test
    void sendOtpShouldRejectFourthRequest() {
        assertRateLimit(3, probe::sendOtp);
    }

    // 4. Verify OTP: 5 requests per 5 minutes
    @Test
    void verifyOtpShouldRejectSixthRequest() {
        assertRateLimit(5, probe::verifyOtp);
    }

    // 5. Forgot password: 3 requests per 15 minutes
    @Test
    void forgotPasswordShouldRejectFourthRequest() {
        assertRateLimit(3, probe::forgotPassword);
    }

    // 6. Reset password: 5 requests per 15 minutes
    @Test
    void resetPasswordShouldRejectSixthRequest() {
        assertRateLimit(5, probe::resetPassword);
    }

    // 7. Refresh token: 10 requests per minute
    @Test
    void refreshTokenShouldRejectEleventhRequest() {
        assertRateLimit(10, probe::refreshToken);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ProbeConfig {

        @Bean
        public RateLimiterProbe rateLimiterProbe() {
            return new RateLimiterProbe();
        }
    }

    // Test-only bean for checking Resilience4j annotations
    public static class RateLimiterProbe {

        @RateLimiter(name = "loginLimiter")
        public void login() {}

        @RateLimiter(name = "registerLimiter")
        public void register() {}

        @RateLimiter(name = "otpSendLimiter")
        public void sendOtp() {}

        @RateLimiter(name = "otpVerifyLimiter")
        public void verifyOtp() {}

        @RateLimiter(name = "forgotPasswordLimiter")
        public void forgotPassword() {}

        @RateLimiter(name = "resetPasswordLimiter")
        public void resetPassword() {}

        @RateLimiter(name = "refreshLimiter")
        public void refreshToken() {}
    }
}
