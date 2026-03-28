package org.example.nihongobackend.controller.user.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nihongobackend.dto.request.auth.LoginRequest;
import org.example.nihongobackend.dto.request.auth.GoogleLoginRequest;
import org.example.nihongobackend.dto.request.auth.ForgotPasswordRequest;
import org.example.nihongobackend.dto.request.auth.ResendVerificationEmailRequest;
import org.example.nihongobackend.dto.request.auth.ResetPasswordRequest;
import org.example.nihongobackend.dto.request.auth.RegisterRequest;
import org.example.nihongobackend.dto.response.auth.AuthUserResponse;
import org.example.nihongobackend.dto.response.auth.LoginResponse;
import org.example.nihongobackend.exception.GlobalExceptionHandler;
import org.example.nihongobackend.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAuthControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        UserAuthController controller = new UserAuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_ShouldReturnSuccessResponse() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@email.com");
        request.setPassword("Secret123");
        request.setConfirmPassword("Secret123");
        request.setName("John");

        AuthUserResponse userResponse = new AuthUserResponse();
        userResponse.setId(UUID.randomUUID());
        userResponse.setEmail("test@email.com");
        userResponse.setName("John");
        userResponse.setRole("USER");
        userResponse.setIsPro(false);

        when(authService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@email.com"));
    }

    @Test
    void login_ShouldReturnSuccessResponse() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@email.com");
        request.setPassword("Secret123");

        AuthUserResponse userResponse = new AuthUserResponse();
        userResponse.setId(UUID.randomUUID());
        userResponse.setEmail("test@email.com");
        userResponse.setName("John");
        userResponse.setRole("USER");
        userResponse.setIsPro(false);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken("jwt-token");
        loginResponse.setUser(userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    void me_ShouldReturnCurrentUser() throws Exception {
        AuthUserResponse userResponse = new AuthUserResponse();
        userResponse.setId(UUID.randomUUID());
        userResponse.setEmail("me@email.com");
        userResponse.setName("Me");
        userResponse.setRole("USER");
        userResponse.setIsPro(false);

        when(authService.me()).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("me@email.com"));
    }

    @Test
    void googleLogin_ShouldReturnSuccessResponse() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("google-id-token");

        AuthUserResponse userResponse = new AuthUserResponse();
        userResponse.setId(UUID.randomUUID());
        userResponse.setEmail("google@email.com");
        userResponse.setName("Google User");
        userResponse.setRole("USER");
        userResponse.setIsPro(false);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken("google-jwt-token");
        loginResponse.setUser(userResponse);

        when(authService.loginWithGoogle(any(GoogleLoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("google-jwt-token"));
    }

    @Test
    void verifyEmail_ShouldReturnSuccessResponse() throws Exception {
        doNothing().when(authService).verifyEmail("raw-token");

        mockMvc.perform(get("/api/auth/verify-email").param("token", "raw-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resendVerification_ShouldReturnSuccessResponse() throws Exception {
        ResendVerificationEmailRequest request = new ResendVerificationEmailRequest();
        request.setEmail("test@email.com");
        doNothing().when(authService).resendVerificationEmail(any(ResendVerificationEmailRequest.class));

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void forgotPassword_ShouldReturnSuccessResponse() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@email.com");
        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resetPassword_ShouldReturnSuccessResponse() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("token-1");
        request.setPassword("Newpass123");
        request.setConfirmPassword("Newpass123");
        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
