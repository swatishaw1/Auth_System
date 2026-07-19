package com.example.authBackend.security.oauth2;

import com.example.authBackend.model.User;
import com.example.authBackend.repository.UserRepository;
import com.example.authBackend.security.CookieService;
import com.example.authBackend.utils.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtUtil;
    private final UserRepository userRepository;
    private final CookieService cookieService;

    @Value("${app.auth.frontend.success-url}")
    private String defaultFrontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        @NonNull Authentication authentication) throws IOException {

        if (response.isCommitted()) {
            log.warn("response already committed cannot redirect after OAuth2 success.");
            return;
        }
        System.out.println(authentication.getPrincipal().getClass());

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // email was normalized into attributes by CustomOAuth2UserService
        assert oAuth2User != null;
        String email = (String) oAuth2User.getAttributes().get("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("OAuth2 user not found in DB after successful login: " + email));

        // reuse your existing JwtUtil create a minimal UserDetails adapter
        if (user == null) {
            throw new IllegalStateException("OAuth2 user not found in DB after successful login: " + email);
        }
        String jwt = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshedToken(user, UUID.randomUUID().toString());
        cookieService.attachAccessTokenCookie(response, jwt, (int) jwtUtil.getAccessTokenValiditySeconds());
        cookieService.attachRefreshCookie(response,refreshToken, (int) jwtUtil.getRefreshTokenValiditySeconds());
        log.info("OAuth2 login successful for [{}] via [{}]", email, user.getProvider());

        // redirect the user's browser to the frontend callback with the jwt, refreshToken
        response.getWriter().write("Login Successful");
        /*getRedirectStrategy().sendRedirect(request, response, defaultFrontendRedirectUri);*/
    }
}