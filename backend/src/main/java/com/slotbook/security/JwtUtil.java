package com.slotbook.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey key;
    public JwtUtil(@Value("${app.jwt.secret}") String secret) { key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }

    public String create(Long userId) {
        return Jwts.builder().subject(String.valueOf(userId))
                .expiration(new Date(System.currentTimeMillis() + 86_400_000L)).signWith(key).compact();
    }
    public Long parse(String token) {
        return Long.parseLong(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());
    }
}
