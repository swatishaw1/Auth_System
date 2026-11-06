package com.example.authBackend.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class CookieServiceTest {

    private CookieService cookieService;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        cookieService = new CookieService(
                "refresh_token",
                true,
                false,
                "example.com",
                "Lax",
                "my_access_cookie"
        );

        response = new MockHttpServletResponse();
    }

    @Test
    void shouldAttachRefreshCookie() {
        cookieService.attachRefreshCookie(
                response, "refresh-jwt", 3600
        );

        String cookie = response.getHeader(HttpHeaders.SET_COOKIE);

        assertNotNull(cookie);
        assertTrue(cookie.contains("refresh_token=refresh-jwt"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Lax"));
        assertTrue(cookie.contains("Max-Age=3600"));
        assertTrue(cookie.contains("Path=/"));
        assertTrue(cookie.contains("Domain=example.com"));
    }

    @Test
    void shouldAttachAccessTokenCookie() {
        cookieService.attachAccessTokenCookie(
                response, "access-jwt", 900
        );

        String cookie = response.getHeader(HttpHeaders.SET_COOKIE2);

        assertNotNull(cookie);
        assertTrue(cookie.contains("access_token=access-jwt"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Lax"));
        assertTrue(cookie.contains("Max-Age=900"));
        assertTrue(cookie.contains("Path=/"));
    }

    @Test
    void shouldClearRefreshCookie() {
        cookieService.clearRefreshCookie(response);

        String cookie = response.getHeader(HttpHeaders.SET_COOKIE);

        assertNotNull(cookie);
        assertTrue(cookie.contains("refresh_token="));
        assertTrue(cookie.contains("Max-Age=0"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Lax"));
    }

    @Test
    void shouldClearAccessCookie() {
        cookieService.clearAccessCookie(response);

        String cookie = response.getHeader(HttpHeaders.SET_COOKIE2);

        assertNotNull(cookie);
        assertTrue(cookie.contains("my_access_cookie="));
        assertTrue(cookie.contains("Max-Age=0"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Lax"));
    }

    @Test
    void shouldAddNoStoreHeaders() {
        cookieService.addNoStoreHeaders(response);

        assertEquals(
                "no-store",
                response.getHeader(HttpHeaders.CACHE_CONTROL)
        );

        assertEquals(
                "no-cache",
                response.getHeader(HttpHeaders.PRAGMA)
        );
    }
}
