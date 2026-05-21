package org.example.nihongobackend.service.impl.admin;

import org.example.nihongobackend.dto.request.auth.LoginRequest;
import org.example.nihongobackend.dto.response.auth.LoginResponse;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.security.AdminJwtService;
import org.example.nihongobackend.service.user.CustomerProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminJwtService adminJwtService;

    @Mock
    private CustomerProfileService customerProfileService;

    private PasswordEncoder passwordEncoder;
    private AdminAuthServiceImpl adminAuthService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        adminAuthService = new AdminAuthServiceImpl(userRepository, passwordEncoder, adminJwtService, customerProfileService);
        lenient().when(customerProfileService.ensureLearnerProfile(any(User.class))).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_success() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setEmail("admin@test.com");
        user.setName("Admin");
        user.setRole("ADMIN");
        user.setIsActive(true);
        user.setPasswordHash(passwordEncoder.encode("Secret123"));

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(adminJwtService.generateToken(eq(id), eq("admin@test.com"), eq("ADMIN"))).thenReturn("admin-jwt");

        LoginRequest req = new LoginRequest();
        req.setEmail("Admin@Test.com");
        req.setPassword("Secret123");

        LoginResponse out = adminAuthService.login(req);
        assertEquals("admin-jwt", out.getToken());
        assertEquals("admin@test.com", out.getUser().getEmail());
        verify(adminJwtService).generateToken(id, "admin@test.com", "ADMIN");
    }

    @Test
    void login_nonAdmin_sameInvalidCredentialsMessage() {
        User user = new User();
        user.setEmail("u@test.com");
        user.setRole("USER");
        user.setIsActive(true);
        user.setPasswordHash(passwordEncoder.encode("x"));

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setEmail("u@test.com");
        req.setPassword("x");

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> adminAuthService.login(req));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void login_wrongPassword() {
        User user = new User();
        user.setEmail("a@test.com");
        user.setRole("ADMIN");
        user.setIsActive(true);
        user.setPasswordHash(passwordEncoder.encode("right"));

        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setEmail("a@test.com");
        req.setPassword("wrong");

        assertThrows(UnauthorizedException.class, () -> adminAuthService.login(req));
    }

    @Test
    void me_success_whenSecurityContextHasAdmin() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("adm@test.com");
        user.setName("A");
        user.setRole("ADMIN");
        user.setIsActive(true);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "adm@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        when(userRepository.findByEmail("adm@test.com")).thenReturn(Optional.of(user));

        assertEquals("adm@test.com", adminAuthService.me().getEmail());
    }

    @Test
    void me_rejectsWhenUserInDbIsNotAdmin() {
        User user = new User();
        user.setEmail("x@test.com");
        user.setRole("USER");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("x@test.com", null, List.of()));

        when(userRepository.findByEmail("x@test.com")).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class, () -> adminAuthService.me());
    }
}
