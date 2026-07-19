package com.example.authBackend.service;

import com.example.authBackend.model.RefreshToken;
import com.example.authBackend.model.User;

public interface RefreshTokenService {

    RefreshToken createOrUpdate(User user, String jti);

    RefreshToken validate(String refreshToken);

    RefreshToken rotate(RefreshToken token, String newJti, String newRefreshToken);

    void revoke(String jti);
}