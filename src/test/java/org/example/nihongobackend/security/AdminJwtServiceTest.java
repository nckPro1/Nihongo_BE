package org.example.nihongobackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminJwtServiceTest {

    private static final String ADMIN_SECRET =
            "admin-jwt-secret-test-key-must-be-at-least-256-bits-long-for-hmac-sha-256-algorithm";

    private AdminJwtService newService() {
        AdminJwtService s = new AdminJwtService();
        ReflectionTestUtils.setField(s, "adminJwtSecret", ADMIN_SECRET);
        ReflectionTestUtils.setField(s, "adminJwtExpirationMs", 3_600_000L);
        return s;
    }

    @Test
    void generateAndValidateAdminToken_ShouldWork() {
        AdminJwtService adminJwtService = newService();
        String email = "admin@test.com";
        String token = adminJwtService.generateToken(UUID.randomUUID(), email, "ADMIN");

        assertTrue(adminJwtService.isTokenValid(token));
        assertEquals(email, adminJwtService.extractEmail(token));
    }

    @Test
    void userJwtFromDifferentSecret_ShouldNotValidateAsAdmin() {
        AdminJwtService adminJwtService = newService();
        JwtService userJwtService = new JwtService();
        ReflectionTestUtils.setField(
                userJwtService,
                "jwtSecret",
                "nihongo-secret-key-2024-very-long-secret-for-jwt-signing-must-be-at-least-256-bits");
        ReflectionTestUtils.setField(userJwtService, "jwtExpirationMs", 3_600_000L);

        String userToken = userJwtService.generateToken(UUID.randomUUID(), "user@test.com", "USER");
        assertFalse(adminJwtService.isTokenValid(userToken));
    }
}
