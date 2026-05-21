package org.example.nihongobackend.service.vocabulary;

/**
 * Exception thrown when user exceeds daily AI lookup quota
 * Free tier: 50 lookups/day
 * Pro tier: Unlimited
 */
public class QuotaExceededException extends RuntimeException {

    public QuotaExceededException(String message) {
        super(message);
    }

    public QuotaExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
