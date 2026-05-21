package org.example.nihongobackend.bootstrap;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * One-time migration: Update old 'USER' role to 'FREE'
 * <p>
 * Legacy users have role='USER' which no longer exists in UserRole enum.
 * This migration updates them to 'FREE' role.
 */
@Component
@Order(50) // Run before AdminDevAccountBootstrap (Order 100)
public class UserRoleMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserRoleMigration.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            int updated = entityManager.createNativeQuery(
                "UPDATE users SET role = 'FREE' WHERE role = 'USER'"
            ).executeUpdate();

            if (updated > 0) {
                log.info("Migrated {} users from role='USER' to role='FREE'", updated);
            } else {
                log.debug("No users with role='USER' found, migration skipped");
            }
        } catch (Exception e) {
            log.error("Failed to migrate user roles", e);
        }
    }
}
