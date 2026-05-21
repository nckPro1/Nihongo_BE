package org.example.nihongobackend.integration.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nihongobackend.bootstrap.AdminDevAccountBootstrap;
import org.example.nihongobackend.dto.request.auth.LoginRequest;
import org.example.nihongobackend.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full stack: {@code AdminJwtAuthenticationFilter} + {@code hasRole("ADMIN")} + DB user (bootstrap admin).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    @AfterEach
    void clearSecurityContextBetweenMvcCalls() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminLogin_returns200AndAdminJwt() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(AdminDevAccountBootstrap.ADMIN_EMAIL);
        req.setPassword(AdminDevAccountBootstrap.DEV_ADMIN_PASSWORD);

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.user.email").value(AdminDevAccountBootstrap.ADMIN_EMAIL));
    }

    @Test
    void adminMe_withoutToken_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminMe_withValidAdminJwt_returns200() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(get("/api/admin/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(AdminDevAccountBootstrap.ADMIN_EMAIL));
    }

    @Test
    void dashboardStats_withValidAdminJwt_returns200() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void usersList_withValidAdminJwt_returns200() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void adminProtected_routesRejectUserAppJwt_evenIfSigned() throws Exception {
        String userJwt = jwtService.generateToken(
                UUID.randomUUID(),
                "anyone@example.com",
                "USER");
        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + userJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminProtected_rejectsGarbageBearer() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .header("Authorization", "Bearer not-a-valid-jwt"))
                .andExpect(status().isForbidden());
    }

    @Test
    void patchUserActive_withValidAdminJwt_returns200() throws Exception {
        String token = loginAndGetToken();
        String adminUserId = adminUserIdFromLogin(token);
        assertThat(adminUserId).isNotBlank();

        mockMvc.perform(patch("/api/admin/users/" + adminUserId + "/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("{\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private String loginAndGetToken() throws Exception {
        SecurityContextHolder.clearContext();
        LoginRequest req = new LoginRequest();
        req.setEmail(AdminDevAccountBootstrap.ADMIN_EMAIL);
        req.setPassword(AdminDevAccountBootstrap.DEV_ADMIN_PASSWORD);
        MvcResult r = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(r.getResponse().getContentAsString());
        String token = root.path("data").path("token").asText();
        SecurityContextHolder.clearContext();
        return token;
    }

    private String adminUserIdFromLogin(String adminJwt) throws Exception {
        SecurityContextHolder.clearContext();
        MvcResult r = mockMvc.perform(get("/api/admin/auth/me")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(r.getResponse().getContentAsString());
        String id = root.path("data").path("id").asText();
        SecurityContextHolder.clearContext();
        return id;
    }
}
