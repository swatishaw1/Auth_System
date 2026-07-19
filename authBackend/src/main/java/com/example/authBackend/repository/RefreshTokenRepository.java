package com.example.authBackend.repository;

import com.example.authBackend.model.RefreshToken;
import com.example.authBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByJti(String jti);

    List<RefreshToken> findAllByUser(User user);

    RefreshToken getRefreshTokenByUser(User user);

    void deleteByUser(User user);
}
