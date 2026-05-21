package org.example.nihongobackend.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nihongobackend.dto.request.auth.LoginRequest;
import org.example.nihongobackend.dto.response.auth.AuthUserResponse;
import org.example.nihongobackend.dto.response.auth.LoginResponse;
import org.example.nihongobackend.exception.GlobalExceptionHandler;
import org.example.nihongobackend.service.admin.AdminAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminAuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AdminAuthService adminAuthService;

    @BeforeEach
    void setUp() {
        adminAuthService = mock(AdminAuthService.class);
        AdminAuthController controller = new AdminAuthController(adminAuthService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void login_returnsToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("pw");

        AuthUserResponse user = new AuthUserResponse();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@test.com");
        user.setName("Admin");
        user.setRole("ADMIN");
        user.setIsPro(false);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken("admin-token");
        loginResponse.setUser(user);

        when(adminAuthService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("admin-token"))
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"));
    }

    @Test
    void me_returnsUser() throws Exception {
        AuthUserResponse user = new AuthUserResponse();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@test.com");
        user.setRole("ADMIN");
        user.setIsPro(false);

        when(adminAuthService.me()).thenReturn(user);

        mockMvc.perform(get("/api/admin/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("admin@test.com"));
    }
}
