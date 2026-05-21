package org.example.nihongobackend.bootstrap;

import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.entity.UserRole;
import org.example.nihongobackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Auto-create admin account on first startup from environment variables.
 * <p>
 * Required env vars:
 * - ADMIN_EMAIL: Admin account email
 * - ADMIN_PASSWORD: Admin account password
 * <p>
 * If admin with ADMIN_EMAIL already exists, does nothing.
 * If env vars not set, logs warning and skips creation.
 */
@Component
@Order(100)
public class AdminDevAccountBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDevAccountBootstrap.class);

    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    @Value("${admin.name:Admin}")
    private String adminName;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminDevAccountBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Check if env vars are set
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.warn("Admin account creation skipped: ADMIN_EMAIL or ADMIN_PASSWORD not set in environment");
            return;
        }

        String email = adminEmail.trim().toLowerCase();

        // Check if admin already exists
        if (userRepository.findByEmail(email).isPresent()) {
            log.info("Admin account already exists: {}", email);
            return;
        }

        // Create admin account
        User admin = new User();
        admin.setEmail(email);
        admin.setName(adminName);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(UserRole.ADMIN);
        admin.setIsActive(true);
        admin.setEmailVerified(true);
        userRepository.save(admin);

        log.info("Admin account created successfully: {}", email);
    }
}
