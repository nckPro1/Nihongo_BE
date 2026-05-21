package org.example.nihongobackend.repository;

import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByIsActiveTrue();

    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find all users except those with ADMIN role
     * For user management UI - admins should not appear in regular user list
     */
    Page<User> findByRoleNotOrderByCreatedAtDesc(UserRole role, Pageable pageable);

    @Query("select count(u) from User u where lower(trim(coalesce(u.role, ''))) = 'admin' and u.isActive = true")
    long countActiveAdmins();
}
