package org.example.nihongobackend.service.impl.admin;

import org.example.nihongobackend.dto.response.admin.AdminUserPageResponse;
import org.example.nihongobackend.dto.response.admin.AdminUserRowResponse;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.entity.UserRole;
import org.example.nihongobackend.exception.BadRequestException;
import org.example.nihongobackend.exception.NotFoundException;
import org.example.nihongobackend.entity.CustomerProfile;
import org.example.nihongobackend.repository.CustomerProfileRepository;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.service.admin.AdminUserManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminUserManagementServiceImpl implements AdminUserManagementService {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;

    public AdminUserManagementServiceImpl(
            UserRepository userRepository,
            CustomerProfileRepository customerProfileRepository) {
        this.userRepository = userRepository;
        this.customerProfileRepository = customerProfileRepository;
    }

    @Override
    public AdminUserPageResponse listUsers(int page, int size) {
        int p = Math.max(0, page);
        int s = Math.min(100, Math.max(1, size));
        // Exclude ADMIN users from the list - they should not appear in user management
        Page<User> result = userRepository.findByRoleNotOrderByCreatedAtDesc(UserRole.ADMIN, PageRequest.of(p, s));
        var ids = result.getContent().stream().map(User::getId).collect(Collectors.toSet());
        Map<UUID, CustomerProfile> profiles = customerProfileRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(CustomerProfile::getId, Function.identity()));

        AdminUserPageResponse out = new AdminUserPageResponse();
        out.setContent(result.getContent().stream().map(u -> toRow(u, profiles.get(u.getId()))).collect(Collectors.toList()));
        out.setTotalElements(result.getTotalElements());
        out.setTotalPages(result.getTotalPages());
        out.setNumber(result.getNumber());
        out.setSize(result.getSize());
        return out;
    }

    @Override
    @Transactional
    public void setUserActive(UUID userId, boolean active) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (isAdmin(target) && !active && userRepository.countActiveAdmins() <= 1) {
            throw new BadRequestException("Cannot deactivate the last active admin");
        }
        target.setIsActive(active);
        userRepository.save(target);
    }

    private AdminUserRowResponse toRow(User u, CustomerProfile profile) {
        AdminUserRowResponse r = new AdminUserRowResponse();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setName(u.getName());
        r.setRole(u.getRole().name());
        String jlpt = profile != null && profile.getJlptLevel() != null && !profile.getJlptLevel().isBlank()
                ? profile.getJlptLevel()
                : "N5";
        r.setJlptLevel(jlpt);
        r.setActive(Boolean.TRUE.equals(u.getIsActive()));
        r.setEmailVerified(Boolean.TRUE.equals(u.getEmailVerified()));
        r.setCreatedAt(u.getCreatedAt());
        return r;
    }

    private static boolean isAdmin(User u) {
        return u.getRole() != null && u.getRole().isAdmin();
    }
}
