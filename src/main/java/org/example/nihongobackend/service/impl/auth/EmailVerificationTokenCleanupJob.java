package org.example.nihongobackend.service.impl.auth;

import org.example.nihongobackend.repository.EmailVerificationTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class EmailVerificationTokenCleanupJob {
    private final EmailVerificationTokenRepository tokenRepository;

    public EmailVerificationTokenCleanupJob(EmailVerificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
