package com.example.authBackend.repository;

import com.example.authBackend.Enum.Provider;
import com.example.authBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.net.http.HttpHeaders;
import java.util.Optional;
import java.util.UUID;

//@Repository//Only when the configuration is Mannual
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}