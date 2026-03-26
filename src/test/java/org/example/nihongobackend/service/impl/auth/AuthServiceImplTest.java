package org.example.nihongobackend.service.impl.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.example.nihongobackend.dto.request.auth.LoginRequest;
import org.example.nihongobackend.dto.request.auth.GoogleLoginRequest;
import org.example.nihongobackend.dto.request.auth.ForgotPasswordRequest;
import org.example.nihongobackend.dto.request.auth.ResendVerificationEmailRequest;
import org.example.nihongobackend.dto.request.auth.ResetPasswordRequest;
import org.example.nihongobackend.dto.request.auth.RegisterRequest;
import org.example.nihongobackend.dto.response.auth.AuthUserResponse;
import org.example.nihongobackend.dto.response.auth.LoginResponse;
import org.example.nihongobackend.entity.EmailVerificationToken;
import org.example.nihongobackend.entity.PasswordResetToken;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.exception.BadRequestException;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.repository.EmailVerificationTokenRepository;
import org.example.nihongobackend.repository.PasswordResetTokenRepository;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.security.JwtService;
import org.example.nihongobackend.service.auth.EmailService;
import org.example.nihongobackend.service.auth.GoogleTokenVerifierService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private GoogleTokenVerifierService googleTokenVerifierService;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_ShouldCreateUser_WhenEmailNotExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("TeSt@Email.com");
        request.setPassword("secret123");
        request.setConfirmPassword("secret123");
        request.setName("  John  ");

        UUID userId = UUID.randomUUID();
        User savedUser = buildUser(userId, "test@email.com", "John", "USER", false, true, false, "hashed");

        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(emailVerificationTokenRepository.findByUserAndUsedAtIsNull(savedUser)).thenReturn(List.of());
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendEmailVerificationLink(eq("test@email.com"), eq("John"), anyString());

        AuthUserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("test@email.com", response.getEmail());
        assertEquals("John", response.getName());
        assertEquals("USER", response.getRole());
        assertFalse(response.getIsPro());
    }

    @Test
    void register_ShouldThrowBadRequest_WhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("taken@email.com");
        request.setPassword("secret123");
        request.setConfirmPassword("secret123");
        request.setName("John");

        when(userRepository.existsByEmail("taken@email.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    void login_ShouldReturnTokenAndUser_WhenCredentialsValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@email.com");
        request.setPassword("secret123");

        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "user@email.com", "Jane", "USER", false, true, true, "hashed");

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(userId, "user@email.com", "USER")).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("user@email.com", response.getUser().getEmail());
    }

    @Test
    void login_ShouldThrowUnauthorized_WhenPasswordInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@email.com");
        request.setPassword("wrong-password");

        User user = buildUser(UUID.randomUUID(), "user@email.com", "Jane", "USER", false, true, true, "hashed");
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void me_ShouldReturnCurrentUser_WhenAuthenticated() {
        String email = "me@email.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null)
        );

        User user = buildUser(UUID.randomUUID(), email, "Me", "USER", true, true, true, "hashed");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        AuthUserResponse response = authService.me();

        assertEquals(email, response.getEmail());
        assertEquals("Me", response.getName());
    }

    @Test
    void me_ShouldThrowUnauthorized_WhenNotAuthenticated() {
        assertThrows(UnauthorizedException.class, () -> authService.me());
    }

    @Test
    void loginWithGoogle_ShouldCreateUser_WhenNotExists() {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("google-id-token");

        UUID userId = UUID.randomUUID();
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("newgoogle@email.com");
        payload.put("name", "Google User");
        payload.setSubject("google-sub-1");

        User savedUser = buildUser(userId, "newgoogle@email.com", "Google User", "USER", false, true, true, null);
        savedUser.setGoogleId("google-sub-1");

        when(googleTokenVerifierService.verify("google-id-token")).thenReturn(payload);
        when(userRepository.findByEmail("newgoogle@email.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(userId, "newgoogle@email.com", "USER")).thenReturn("google-jwt");

        LoginResponse response = authService.loginWithGoogle(request);

        assertEquals("google-jwt", response.getToken());
        assertEquals("newgoogle@email.com", response.getUser().getEmail());
    }

    @Test
    void loginWithGoogle_ShouldReturnExistingUser_WhenExists() {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("google-id-token");

        UUID userId = UUID.randomUUID();
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("existing@email.com");
        payload.setSubject("google-sub-2");

        User existingUser = buildUser(userId, "existing@email.com", "Existing", "USER", false, true, true, null);
        existingUser.setGoogleId("google-sub-2");

        when(googleTokenVerifierService.verify("google-id-token")).thenReturn(payload);
        when(userRepository.findByEmail("existing@email.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(userId, "existing@email.com", "USER")).thenReturn("existing-jwt");

        LoginResponse response = authService.loginWithGoogle(request);

        assertEquals("existing-jwt", response.getToken());
        assertEquals("existing@email.com", response.getUser().getEmail());
    }

    @Test
    void login_ShouldThrowUnauthorized_WhenEmailNotVerified() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@email.com");
        request.setPassword("secret123");

        User user = buildUser(UUID.randomUUID(), "user@email.com", "Jane", "USER", false, true, false, "hashed");
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void verifyEmail_ShouldMarkUserVerified_WhenTokenValid() {
        User user = buildUser(UUID.randomUUID(), "verify@email.com", "Verify", "USER", false, true, false, "hashed");
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(5));

        when(emailVerificationTokenRepository.findByTokenHashAndUsedAtIsNull(anyString())).thenReturn(Optional.of(token));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class))).thenReturn(token);

        authService.verifyEmail("raw-token");

        verify(userRepository).save(any(User.class));
        verify(emailVerificationTokenRepository).save(any(EmailVerificationToken.class));
    }

    @Test
    void resendVerificationEmail_ShouldSendMail_WhenUserNotVerified() {
        ResendVerificationEmailRequest request = new ResendVerificationEmailRequest();
        request.setEmail("verify@email.com");

        User user = buildUser(UUID.randomUUID(), "verify@email.com", "Verify", "USER", false, true, false, "hashed");
        when(userRepository.findByEmail("verify@email.com")).thenReturn(Optional.of(user));
        when(emailVerificationTokenRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());
        when(emailVerificationTokenRepository.findByUserAndUsedAtIsNull(user)).thenReturn(List.of());
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendEmailVerificationLink(eq("verify@email.com"), eq("Verify"), anyString());

        authService.resendVerificationEmail(request);

        verify(emailService).sendEmailVerificationLink(eq("verify@email.com"), eq("Verify"), anyString());
    }

    @Test
    void resendVerificationEmail_ShouldNotReveal_WhenUserNotFound() {
        ResendVerificationEmailRequest request = new ResendVerificationEmailRequest();
        request.setEmail("missing@email.com");
        when(userRepository.findByEmail("missing@email.com")).thenReturn(Optional.empty());

        authService.resendVerificationEmail(request);
    }

    @Test
    void resendVerificationEmail_ShouldThrowBadRequest_WhenCooldownNotReached() {
        ResendVerificationEmailRequest request = new ResendVerificationEmailRequest();
        request.setEmail("verify@email.com");

        User user = buildUser(UUID.randomUUID(), "verify@email.com", "Verify", "USER", false, true, false, "hashed");
        EmailVerificationToken latestToken = new EmailVerificationToken();
        latestToken.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("verify@email.com")).thenReturn(Optional.of(user));
        when(emailVerificationTokenRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(latestToken));

        assertThrows(BadRequestException.class, () -> authService.resendVerificationEmail(request));
    }

    @Test
    void forgotPassword_ShouldSendMail_WhenUserExists() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("reset@email.com");

        User user = buildUser(UUID.randomUUID(), "reset@email.com", "Reset", "USER", false, true, true, "hashed");
        when(userRepository.findByEmail("reset@email.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());
        when(passwordResetTokenRepository.findByUserAndUsedAtIsNull(user)).thenReturn(List.of());
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendResetPasswordLink(eq("reset@email.com"), eq("Reset"), anyString());

        authService.forgotPassword(request);

        verify(emailService).sendResetPasswordLink(eq("reset@email.com"), eq("Reset"), anyString());
    }

    @Test
    void forgotPassword_ShouldNotReveal_WhenUserNotFound() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@email.com");
        when(userRepository.findByEmail("missing@email.com")).thenReturn(Optional.empty());

        authService.forgotPassword(request);
    }

    @Test
    void resetPassword_ShouldUpdatePassword_WhenTokenValid() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("raw-reset-token");
        request.setPassword("newpass123");
        request.setConfirmPassword("newpass123");

        User user = buildUser(UUID.randomUUID(), "reset@email.com", "Reset", "USER", false, true, true, "oldHash");
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(passwordResetTokenRepository.findByTokenHashAndUsedAtIsNull(anyString())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newpass123")).thenReturn("newHash");

        authService.resetPassword(request);

        verify(userRepository).save(any(User.class));
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    private User buildUser(UUID id, String email, String name, String role, boolean isPro, boolean isActive, boolean emailVerified, String passwordHash) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        user.setIsPro(isPro);
        user.setIsActive(isActive);
        user.setEmailVerified(emailVerified);
        user.setPasswordHash(passwordHash);
        return user;
    }
}
