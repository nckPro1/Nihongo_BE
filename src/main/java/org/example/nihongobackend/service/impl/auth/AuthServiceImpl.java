package org.example.nihongobackend.service.impl.auth;

import org.example.nihongobackend.dto.request.auth.LoginRequest;
import org.example.nihongobackend.dto.request.auth.GoogleLoginRequest;
import org.example.nihongobackend.dto.request.auth.GoogleCodeLoginRequest;
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
import org.example.nihongobackend.mapper.UserResponseMapper;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.repository.EmailVerificationTokenRepository;
import org.example.nihongobackend.repository.PasswordResetTokenRepository;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.security.JwtService;
import org.example.nihongobackend.security.PasswordPolicy;
import org.example.nihongobackend.service.auth.AuthService;
import org.example.nihongobackend.service.auth.EmailService;
import org.example.nihongobackend.service.auth.GoogleTokenVerifierService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private static final long RESEND_COOLDOWN_SECONDS = 60;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            GoogleTokenVerifierService googleTokenVerifierService,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleTokenVerifierService = googleTokenVerifierService;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public AuthUserResponse register(RegisterRequest request) {
        PasswordPolicy.validateOrThrow(request.getPassword());
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }

        String email = request.getEmail().trim().toLowerCase();
        User existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser != null) {
            // Sync case: account was created via Google first, user now wants password login too.
            if (existingUser.getGoogleId() != null && !existingUser.getGoogleId().isBlank()
                    && (existingUser.getPasswordHash() == null || existingUser.getPasswordHash().isBlank())) {
                existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                if (request.getName() != null && !request.getName().trim().isBlank()) {
                    existingUser.setName(request.getName().trim());
                }
                User savedUser = userRepository.save(existingUser);
                return UserResponseMapper.toAuthUserResponse(savedUser);
            }
            throw new BadRequestException("Email is already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setName(request.getName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setIsActive(true);
        user.setIsPro(false);
        user.setEmailVerified(false);

        User saved = userRepository.save(user);
        createAndSendVerificationToken(saved);
        return UserResponseMapper.toAuthUserResponse(saved);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Account is inactive");
        }
        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw new UnauthorizedException("Please verify your email before login");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new UnauthorizedException("This account uses Google login. Please continue with Google or reset your password.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(UserResponseMapper.toAuthUserResponse(user));
        return response;
    }

    @Override
    @Transactional
    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        var payload = googleTokenVerifierService.verify(request.getIdToken().trim());
        return loginFromGooglePayload(payload);
    }

    @Override
    @Transactional
    public LoginResponse loginWithGoogleCode(GoogleCodeLoginRequest request) {
        var payload = googleTokenVerifierService.verifyAuthorizationCode(request.getCode().trim(), request.getRedirectUri().trim());
        return loginFromGooglePayload(payload);
    }

    private LoginResponse loginFromGooglePayload(com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Google account email is missing");
        }

        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(normalizedEmail);
                    String fullName = (String) payload.get("name");
                    newUser.setName((fullName == null || fullName.isBlank()) ? "Google User" : fullName.trim());
                    newUser.setGoogleId(payload.getSubject());
                    applyGoogleProfilePicture(newUser, payload);
                    newUser.setRole("USER");
                    newUser.setIsActive(true);
                    newUser.setIsPro(false);
                    newUser.setEmailVerified(true);
                    return userRepository.save(newUser);
                });

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Account is inactive");
        }

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            user.setEmailVerified(true);
        }

        if (user.getGoogleId() != null && !user.getGoogleId().isBlank()
                && payload.getSubject() != null && !user.getGoogleId().equals(payload.getSubject())) {
            throw new UnauthorizedException("Google account does not match this email");
        }
        if ((user.getGoogleId() == null || user.getGoogleId().isBlank()) && payload.getSubject() != null) {
            user.setGoogleId(payload.getSubject());
        }
        applyGoogleProfilePicture(user, payload);
        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(UserResponseMapper.toAuthUserResponse(user));
        return response;
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || Boolean.FALSE.equals(user.getIsActive())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        var latestToken = passwordResetTokenRepository.findTopByUserOrderByCreatedAtDesc(user);
        if (latestToken.isPresent() && latestToken.get().getCreatedAt() != null
                && latestToken.get().getCreatedAt().plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(now)) {
            throw new BadRequestException("Please wait 60 seconds before requesting another reset email");
        }

        createAndSendResetPasswordToken(user);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordPolicy.validateOrThrow(request.getPassword());
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }

        String tokenHash = hashToken(request.getToken().trim());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHashAndUsedAtIsNull(tokenHash)
                .orElseThrow(() -> new BadRequestException("Reset password token is invalid"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset password token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        String tokenHash = hashToken(token);
        EmailVerificationToken verifyToken = emailVerificationTokenRepository
                .findByTokenHashAndUsedAtIsNull(tokenHash)
                .orElseThrow(() -> new BadRequestException("Verification token is invalid"));

        if (verifyToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token has expired");
        }

        User user = verifyToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verifyToken.setUsedAt(LocalDateTime.now());
        emailVerificationTokenRepository.save(verifyToken);
    }

    @Override
    @Transactional
    public void resendVerificationEmail(ResendVerificationEmailRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || Boolean.TRUE.equals(user.getEmailVerified())) {
            // Do not reveal whether email exists or not.
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        var latestToken = emailVerificationTokenRepository.findTopByUserOrderByCreatedAtDesc(user);
        if (latestToken.isPresent() && latestToken.get().getCreatedAt() != null
                && latestToken.get().getCreatedAt().plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(now)) {
            throw new BadRequestException("Please wait 60 seconds before requesting another verification email");
        }

        createAndSendVerificationToken(user);
    }

    @Override
    public AuthUserResponse me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return UserResponseMapper.toAuthUserResponse(user);
    }

    private void applyGoogleProfilePicture(User user, com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload) {
        Object picture = payload.get("picture");
        if (picture instanceof String s && !s.isBlank()) {
            user.setAvatar(s.trim());
        }
    }

    private void createAndSendVerificationToken(User user) {
        for (EmailVerificationToken activeToken : emailVerificationTokenRepository.findByUserAndUsedAtIsNull(user)) {
            activeToken.setUsedAt(LocalDateTime.now());
            emailVerificationTokenRepository.save(activeToken);
        }

        String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        emailVerificationTokenRepository.save(token);

        String verificationLink = frontendBaseUrl + "/verify-email?token=" + rawToken;
        emailService.sendEmailVerificationLink(user.getEmail(), user.getName(), verificationLink);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot hash verification token");
        }
    }

    private void createAndSendResetPasswordToken(User user) {
        for (PasswordResetToken activeToken : passwordResetTokenRepository.findByUserAndUsedAtIsNull(user)) {
            activeToken.setUsedAt(LocalDateTime.now());
            passwordResetTokenRepository.save(activeToken);
        }

        String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(hashToken(rawToken));
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendBaseUrl + "/reset-password?token=" + rawToken;
        emailService.sendResetPasswordLink(user.getEmail(), user.getName(), resetLink);
    }

}
