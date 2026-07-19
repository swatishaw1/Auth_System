package com.example.authBackend.service.implementation;

import com.example.authBackend.Enum.Role;
import com.example.authBackend.api.request.LoginRequest;
import com.example.authBackend.api.response.TokenResponse;
import com.example.authBackend.api.request.RefreshTokenRequest;
import com.example.authBackend.dto.UserDTO;
import com.example.authBackend.model.RefreshToken;
import com.example.authBackend.model.User;
import com.example.authBackend.repository.UserRepository;
import com.example.authBackend.security.CookieService;
import com.example.authBackend.service.AuthenticationService;
import com.example.authBackend.service.RefreshTokenService;
import com.example.authBackend.utils.JwtService;
import com.example.authBackend.service.AuthService;
import com.example.authBackend.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final CookieService cookieService;

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        return userService.createUser(userDTO);
    }

    @Override
    public TokenResponse loginUser(LoginRequest loginRequest, HttpServletResponse response) {
        authenticationService.authenticate(loginRequest.email(), loginRequest.password());
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() ->
                new BadCredentialsException("Invalid Email"));
        if (!user.isEnabled()) {
            throw new BadCredentialsException("User is disabled");
        }
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        log.info("User {} logged in", user.getId());
        String jti = UUID.randomUUID().toString();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshedToken(user, jti);
        refreshTokenService.createOrUpdate(user, jti);
        cookieService.attachRefreshCookie(response,refreshToken,(int)jwtService.getRefreshTokenValiditySeconds());
        cookieService.attachAccessTokenCookie(response,accessToken,(int)jwtService.getAccessTokenValiditySeconds()
        );
        cookieService.addNoStoreHeaders(response);
        return TokenResponse.of(accessToken,refreshToken, jwtService.getAccessTokenValiditySeconds(),
                modelMapper.map(user, UserDTO.class)
        );
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest body,HttpServletRequest request,HttpServletResponse response) {
        String refreshToken = readRefreshTokenFromRequest(body, request).orElseThrow(() ->  new BadCredentialsException("Refresh Token Missing"));
        RefreshToken storedToken = refreshTokenService.validate(refreshToken);
        User user = storedToken.getUser();
        String newJti = UUID.randomUUID().toString();
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshedToken(user, newJti);
        refreshTokenService.rotate(storedToken,newJti, newRefreshToken);
        cookieService.attachRefreshCookie(response, newRefreshToken, (int) jwtService.getRefreshTokenValiditySeconds());
        cookieService.attachAccessTokenCookie(response, newAccessToken,(int) jwtService.getAccessTokenValiditySeconds());
        cookieService.addNoStoreHeaders(response);
        return TokenResponse.of(newAccessToken, newRefreshToken,
                jwtService.getAccessTokenValiditySeconds(), modelMapper.map(user, UserDTO.class));
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        readRefreshTokenFromRequest(null, request).ifPresent(token -> {
            try {
                if (jwtService.isRefreshToken(token)) {
                    String jti = jwtService.getJwtId(token);
                    refreshTokenService.revoke(jti);
                }
            } catch (JwtException ignored) {
                log.error("Error while processing Refresh Token Message:{}", ignored.getMessage());
            }
        });
        cookieService.clearRefreshCookie(response);
        cookieService.clearAccessCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
    }

    //Read data from the refresh token from the body
    public Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request){
        if (request.getCookies()!=null){
            Optional<String> fromCookie= Arrays.stream(request.getCookies())
                    .filter(c-> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> v!=null && !v.isBlank()).findFirst();
            if (fromCookie.isPresent()){
                return fromCookie;
            }
        }
        //2. Body
        if (body!=null && body.refreshToken()!=null && !body.refreshToken().isBlank()){
            return Optional.of(body.refreshToken().trim());
        }
        //3. Custome Header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader!=null && !refreshHeader.isBlank()){
            return Optional.of(refreshHeader.trim());
        }
        //Authorization = Bearer <token>
        String authHeader =  request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader!=null && !authHeader.regionMatches(true,0,"Bearer ",0,7)) {
            String candidate = authHeader.substring(7).trim();
            if (!candidate.isEmpty()) {
                try {
                    if (jwtService.isRefreshToken(candidate)) {
                        return Optional.of(candidate);
                    }else {
                        return Optional.empty();
                    }
                } catch (Exception ignored) {
                    throw new BadCredentialsException("Unauthorized: "+ignored.getMessage());
                }
            }
        }
        return Optional.empty();
    }
}