package org.example.nihongobackend.service.auth;

import org.example.nihongobackend.dto.request.auth.LoginRequest;
import org.example.nihongobackend.dto.request.auth.GoogleLoginRequest;
import org.example.nihongobackend.dto.request.auth.GoogleCodeLoginRequest;
import org.example.nihongobackend.dto.request.auth.ForgotPasswordRequest;
import org.example.nihongobackend.dto.request.auth.ResendVerificationEmailRequest;
import org.example.nihongobackend.dto.request.auth.ResetPasswordRequest;
import org.example.nihongobackend.dto.request.auth.RegisterRequest;
import org.example.nihongobackend.dto.response.auth.AuthUserResponse;
import org.example.nihongobackend.dto.response.auth.LoginResponse;

public interface AuthService {
    AuthUserResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse loginWithGoogle(GoogleLoginRequest request);
    LoginResponse loginWithGoogleCode(GoogleCodeLoginRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    void verifyEmail(String token);
    void resendVerificationEmail(ResendVerificationEmailRequest request);
    AuthUserResponse me();
}
