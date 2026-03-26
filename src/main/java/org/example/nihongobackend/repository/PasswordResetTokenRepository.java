package org.example.nihongobackend.repository;

import org.example.nihongobackend.entity.PasswordResetToken;
import org.example.nihongobackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNull(String tokenHash);
    Optional<PasswordResetToken> findTopByUserOrderByCreatedAtDesc(User user);
    List<PasswordResetToken> findByUserAndUsedAtIsNull(User user);
    long deleteByExpiresAtBefore(LocalDateTime now);
}
