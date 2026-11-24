package com.yashin.userservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

// JWT подписывается сервисом своим ключом и проверяется локально —
// другие сервисы смогут валидировать токен этим же ключом без похода сюда (лаба 5)
@Component
public class JwtUtil {

    private final SecretKey key;
    private final Duration ttl;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.ttl-hours}") long ttlHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttl = Duration.ofHours(ttlHours);
    }

    public String generate(Long userId, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    public Long parseUserId(String token) {
        Claims claims = parse(token);
        return Long.valueOf(claims.getSubject());
    }

    @SuppressWarnings("unchecked")
    public List<String> parseRoles(String token) {
        Claims claims = parse(token);
        return (List<String>) claims.get("roles", List.class);
    }

    // expiresAt для ответа логина — время, когда истечёт только что выданный токен
    public OffsetDateTime expiresAt() {
        return OffsetDateTime.ofInstant(Instant.now().plus(ttl), ZoneId.systemDefault());
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
