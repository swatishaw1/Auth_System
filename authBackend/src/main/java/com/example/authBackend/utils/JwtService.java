package com.example.authBackend.utils;

import com.example.authBackend.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Data
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;
    private final String issuer;

    public JwtService(@Value("${security.jwt.secret}") String secretKey,
                      @Value("${security.jwt.access-ttl-seconds}") long accessTokenValiditySeconds,
                      @Value("${security.jwt.refresh-ttl-seconds}") long refreshTokenValiditySeconds,
                      @Value("${security.jwt.issuer}") String issuer) {
        if (secretKey == null || secretKey.length() < 64) {
            throw new IllegalArgumentException("Secret key must be at least 64 characters long");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
        this.issuer = issuer;
    }

    public String generateAccessToken(User user){
        Instant now = Instant.now();
        List<String> roles = user.getRole()==null? List.of(): List.of(user.getRole().toString());
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenValiditySeconds)))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "roles",roles,
                        "typ","access"
                ))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshedToken(User user,String jti){
        Instant now = Instant.now();
        List<String> roles = user.getRole()==null? List.of(): List.of(user.getRole().toString());
        return Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTokenValiditySeconds)))
                .claim("typ","refresh")
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public Jws<Claims> parse(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
    }

    public boolean isAccessToken(String token){
        Claims c = parse(token).getPayload();
        return "access".equals(c.get("typ"));
    }

    public boolean isRefreshToken(String token){
        Claims c = parse(token).getPayload();
        return "refresh".equals(c.get("typ"));
    }

    public UUID getUserId(String token){
        Claims c = parse(token).getPayload();
        return  UUID.fromString(c.getSubject());
    }

    public String getJwtId(String token){
        return parse(token).getPayload().getId();
    }

    public List<String> getRoles(String token){
        Claims c = parse(token).getPayload();
        return c.get("roles", List.class);
    }
}