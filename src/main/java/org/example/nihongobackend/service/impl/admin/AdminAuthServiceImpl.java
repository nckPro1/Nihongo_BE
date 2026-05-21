package org.example.nihongobackend.service.impl.admin;

import org.example.nihongobackend.dto.request.auth.LoginRequest;
import org.example.nihongobackend.dto.response.auth.AuthUserResponse;
import org.example.nihongobackend.dto.response.auth.LoginResponse;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.mapper.UserResponseMapper;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.security.AdminJwtService;
import org.example.nihongobackend.service.admin.AdminAuthService;
import org.example.nihongobackend.service.user.CustomerProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminJwtService adminJwtService;
    private final CustomerProfileService customerProfileService;

    public AdminAuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AdminJwtService adminJwtService,
            CustomerProfileService customerProfileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminJwtService = adminJwtService;
        this.customerProfileService = customerProfileService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.getRole().isAdmin()) {
            throw new UnauthorizedException("Invalid credentials");
        }
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new UnauthorizedException("Invalid credentials");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = adminJwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        var profile = customerProfileService.ensureLearnerProfile(user);
        response.setUser(UserResponseMapper.toAuthUserResponse(user, profile));
        return response;
    }

    @Override
    public AuthUserResponse me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));
        if (!user.getRole().isAdmin()) {
            throw new UnauthorizedException("Unauthorized");
        }
        var profile = customerProfileService.ensureLearnerProfile(user);
        return UserResponseMapper.toAuthUserResponse(user, profile);
    }
}
