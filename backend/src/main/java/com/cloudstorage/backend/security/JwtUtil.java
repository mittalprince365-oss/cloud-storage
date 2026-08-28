package com.cloudstorage.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // secret key (production mein .env se aayega)
    private final SecretKey key = Keys.hmacShaKeyFor(
        "my-super-secret-jwt-key-for-cloud-storage-app-123456".getBytes()
    );

    private final long EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7 din

    // token banao
    public String generateToken(Long userId, String email) {
        return Jwts.builder()
            .subject(email)
            .claim("userId", userId)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
            .signWith(key)
            .compact();
    }

    // token se email nikaalo
    public String getEmailFromToken(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    // token valid hai?
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}