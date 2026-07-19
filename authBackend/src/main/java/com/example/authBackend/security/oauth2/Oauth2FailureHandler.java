package com.example.authBackend.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class Oauth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.auth.frontend.failure-url}")
    private String frontendUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        ResponseCookie errorCookie = ResponseCookie.from("oauth_error",
                        URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8))
                .httpOnly(false)
                .secure(false) // true in production
                .path("/")
                .maxAge(60)
                .sameSite("Lax")
                .build();
        log.error(exception.getMessage());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        response.getWriter().write(
                """
                {
                    "error":"%s"
                }
                """.formatted(exception.getMessage())
        );


        response.addHeader(HttpHeaders.SET_COOKIE, errorCookie.toString());

        response.getWriter().write("Login failure");
        /*getRedirectStrategy().sendRedirect(request, response, frontendUri);*/
    }
}
