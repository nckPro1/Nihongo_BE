package org.example.nihongobackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void generateAndValidateToken_ShouldWork() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "nihongo-secret-key-2024-very-long-secret-for-jwt-signing-must-be-at-least-256-bits");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);

        String email = "jwt@test.com";
        String token = jwtService.generateToken(UUID.randomUUID(), email, "USER");

        assertTrue(jwtService.isTokenValid(token));
        assertEquals(email, jwtService.extractEmail(token));
    }
}
