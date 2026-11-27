package com.example.placement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expiration;

    public JwtService(@Value("${app.jwt.secret:}") String secret,
                      @Value("${app.jwt.expiration-ms:86400000}") long expiration) {
        if (secret == null || secret.trim().length() < 32) {
            throw new IllegalArgumentException("JWT_SECRET is required and must contain at least 32 characters. Set it before starting the backend.");
        }
        if (expiration < 60000) throw new IllegalArgumentException("JWT_EXPIRATION_MS must be at least 60000 milliseconds.");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generate(String email, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder().subject(email).claim("role", role)
            .issuedAt(new Date(now)).expiration(new Date(now + expiration))
            .signWith(key).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
