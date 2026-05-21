package org.example.nihongobackend.service.impl.user;

import org.example.nihongobackend.dto.request.user.ChangePasswordRequest;
import org.example.nihongobackend.dto.request.user.UpdateProfileRequest;
import org.example.nihongobackend.dto.response.auth.AuthUserResponse;
import org.example.nihongobackend.dto.response.media.MediaUploadResponse;
import org.example.nihongobackend.dto.response.user.TransactionItemResponse;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.exception.BadRequestException;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.mapper.UserResponseMapper;
import org.example.nihongobackend.entity.CustomerProfile;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.service.user.CustomerProfileService;
import org.example.nihongobackend.security.PasswordPolicy;
import org.example.nihongobackend.service.media.CloudinaryMediaService;
import org.example.nihongobackend.service.user.UserProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final CloudinaryMediaService cloudinaryMediaService;
    private final PasswordEncoder passwordEncoder;
    private final CustomerProfileService customerProfileService;

    public UserProfileServiceImpl(
            UserRepository userRepository,
            CloudinaryMediaService cloudinaryMediaService,
            PasswordEncoder passwordEncoder,
            CustomerProfileService customerProfileService
    ) {
        this.userRepository = userRepository;
        this.cloudinaryMediaService = cloudinaryMediaService;
        this.passwordEncoder = passwordEncoder;
        this.customerProfileService = customerProfileService;
    }

    @Override
    @Transactional
    public AuthUserResponse updateAvatar(MultipartFile file) {
        User user = loadCurrentUser();
        MediaUploadResponse upload = cloudinaryMediaService.uploadImage(file, "avatars");
        user.setAvatar(upload.getSecureUrl());
        userRepository.save(user);
        CustomerProfile profile = customerProfileService.ensureLearnerProfile(user);
        return UserResponseMapper.toAuthUserResponse(user, profile);
    }

    @Override
    @Transactional
    public AuthUserResponse updateProfile(UpdateProfileRequest request) {
        User user = loadCurrentUser();
        boolean touched = false;
        if (request.getName() != null) {
            String n = request.getName().trim();
            if (n.isEmpty()) {
                throw new BadRequestException("Name cannot be empty");
            }
            user.setName(n);
            touched = true;
        }
        CustomerProfile profile = null;
        if (request.getJlptLevel() != null) {
            profile = customerProfileService.ensureLearnerProfile(user);
            if (profile == null) {
                throw new BadRequestException("JLPT level cannot be updated for this account");
            }
            profile.setJlptLevel(request.getJlptLevel().trim());
            customerProfileService.save(profile);
            touched = true;
        }
        if (!touched) {
            throw new BadRequestException("No fields to update");
        }
        userRepository.save(user);
        if (profile == null) {
            profile = customerProfileService.ensureLearnerProfile(user);
        }
        return UserResponseMapper.toAuthUserResponse(user, profile);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = loadCurrentUser();
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new BadRequestException("This account uses Google sign-in only. Password change is not available.");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }
        PasswordPolicy.validateOrThrow(request.getNewPassword());
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public List<TransactionItemResponse> listTransactions() {
        loadCurrentUser();
        return Collections.emptyList();
    }

    private User loadCurrentUser() {
        String email = currentEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        return authentication.getName();
    }
}
