package org.example.nihongobackend.repository;

import org.example.nihongobackend.entity.EmailVerificationToken;
import org.example.nihongobackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByTokenHashAndUsedAtIsNull(String tokenHash);
    List<EmailVerificationToken> findByUserAndUsedAtIsNull(User user);
    Optional<EmailVerificationToken> findTopByUserOrderByCreatedAtDesc(User user);
    long deleteByExpiresAtBefore(LocalDateTime now);
}
