package org.example.nihongobackend.controller.user.auth;

import jakarta.validation.Valid;
import org.example.nihongobackend.dto.request.auth.LoginRequest;
import org.example.nihongobackend.dto.request.auth.GoogleLoginRequest;
import org.example.nihongobackend.dto.request.auth.ForgotPasswordRequest;
import org.example.nihongobackend.dto.request.auth.ResendVerificationEmailRequest;
import org.example.nihongobackend.dto.request.auth.ResetPasswordRequest;
import org.example.nihongobackend.dto.request.auth.RegisterRequest;
import org.example.nihongobackend.dto.response.auth.AuthUserResponse;
import org.example.nihongobackend.dto.response.auth.LoginResponse;
import org.example.nihongobackend.dto.response.common.ApiResponse;
import org.example.nihongobackend.service.auth.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserAuthController {
    private final AuthService authService;

    public UserAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthUserResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthUserResponse data = authService.register(request);
        return ApiResponse.success("Register successfully", data);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse data = authService.login(request);
        return ApiResponse.success("Login successfully", data);
    }

    @PostMapping("/google")
    public ApiResponse<LoginResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        LoginResponse data = authService.loginWithGoogle(request);
        return ApiResponse.success("Google login successfully", data);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Object> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.success("If this email exists, a reset link has been sent", null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Object> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("Password has been reset successfully", null);
    }

    @GetMapping("/verify-email")
    public ApiResponse<Object> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ApiResponse.success("Email verified successfully", null);
    }

    @PostMapping("/resend-verification")
    public ApiResponse<Object> resendVerificationEmail(@Valid @RequestBody ResendVerificationEmailRequest request) {
        authService.resendVerificationEmail(request);
        return ApiResponse.success("Verification email has been sent", null);
    }

    @GetMapping("/me")
    public ApiResponse<AuthUserResponse> me() {
        AuthUserResponse data = authService.me();
        return ApiResponse.success("Get profile successfully", data);
    }
}
