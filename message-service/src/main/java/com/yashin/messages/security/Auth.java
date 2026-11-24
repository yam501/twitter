package com.yashin.messages.security;

import com.yashin.messages.api.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class Auth {
    private final SecretKey key;
    public Auth(@Value("${jwt.secret}") String secret) {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    public Actor required(String header) {
        Actor actor = optional(header);
        if (actor == null) throw new ApiException(401, "Требуется авторизация");
        return actor;
    }
    public Actor optional(String header) {
        if (header == null) return null;
        if (!header.startsWith("Bearer ")) throw new ApiException(401, "Ожидается Bearer-токен");
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(header.substring(7)).getPayload();
            Long id = Long.valueOf(claims.getSubject());
            Object rawRoles = claims.get("roles");
            if (id <= 0 || claims.getExpiration() == null || !(rawRoles instanceof List<?> roles)
                    || roles.stream().anyMatch(role -> !(role instanceof String))) {
                throw new IllegalArgumentException();
            }
            return new Actor(id, roles.stream().map(String.class::cast).toList());
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(401, "Токен недействителен или истёк");
        }
    }
}
