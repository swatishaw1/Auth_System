package com.example.authBackend.service.implementation;


import com.example.authBackend.model.RefreshToken;
import com.example.authBackend.model.User;
import com.example.authBackend.repository.RefreshTokenRepository;
import com.example.authBackend.service.RefreshTokenService;
import com.example.authBackend.utils.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Override
    public RefreshToken createOrUpdate(User user, String jti) {
        RefreshToken refreshToken = refreshTokenRepository.getRefreshTokenByUser(user);
        if (refreshToken == null) {
            refreshToken = new RefreshToken();
        }
        refreshToken.setUser(user);
        refreshToken.setJti(jti);
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plusSeconds(jwtService.getRefreshTokenValiditySeconds()));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken validate(String token) {
        if (!jwtService.isRefreshToken(token)) {
            throw new BadCredentialsException("Invalid Refresh Token");
        }
        String jti = jwtService.getJwtId(token);
        RefreshToken refreshToken = refreshTokenRepository.findByJti(jti).orElseThrow(() ->
                                new BadCredentialsException("Refresh Token Not Found"));
        if (refreshToken.isRevoked()) {
            throw new BadCredentialsException("Refresh Token Revoked");
        }
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh Token Expired");
        }
        return refreshToken;
    }

    @Override
    public RefreshToken rotate(RefreshToken token,String newJti, String newRefreshToken){
        token.setJti(newJti);
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plusSeconds(jwtService.getRefreshTokenValiditySeconds()));
        token.setReplacedByToken(newRefreshToken);
        return refreshTokenRepository.save(token);
    }

    @Override
    public void revoke(String jti) {
        refreshTokenRepository.findByJti(jti).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
}