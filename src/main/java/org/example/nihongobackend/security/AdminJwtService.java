package org.example.nihongobackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.nihongobackend.entity.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT ký bằng secret riêng với luồng user — token user không dùng được cho /api/admin/**.
 */
@Service
public class AdminJwtService {
    @Value("${admin.jwt.secret}")
    private String adminJwtSecret;

    @Value("${admin.jwt.expiration}")
    private long adminJwtExpirationMs;

    public String generateToken(UUID userId, String email, UserRole role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + adminJwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("role", role.name())
                .claim("scope", "admin")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            if (!"admin".equals(claims.get("scope"))) {
                return false;
            }
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(adminJwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
