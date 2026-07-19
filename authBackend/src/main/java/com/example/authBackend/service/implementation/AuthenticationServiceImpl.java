package com.example.authBackend.service.implementation;

import com.example.authBackend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;

    @Override
    public Authentication authenticate(String email, String password) {
        try {
            /*String provider = loginRequest.provider();
            switch (provider.toLowerCase()) {
                case "google" -> {
                    response.sendRedirect("http://localhost:8080/oauth2/authorization/google");
                    return;
                }

                case "github" -> {
                    response.sendRedirect("http://localhost:8080/oauth2/authorization/github");
                    return;
                }

                default -> {
                    return authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken( loginRequest.email(),
                                                                    loginRequest.password()));
                }
            }*/
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid Email or Password");
        }
    }
}