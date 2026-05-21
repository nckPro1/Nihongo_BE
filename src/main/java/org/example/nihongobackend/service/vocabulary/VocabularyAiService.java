package org.example.nihongobackend.service.vocabulary;

import org.example.nihongobackend.dto.response.vocabulary.VocabularyLookupResponse;

/**
 * Layer 4: AI Service for vocabulary translation
 * - Uses DeepSeek/Gemini for Japanese → Vietnamese translation
 * - Implements quota system (50 free, unlimited Pro)
 * - Auto-saves results to Layer 3 (AI Cache)
 */
public interface VocabularyAiService {

    /**
     * Translate Japanese word/phrase to Vietnamese using AI
     *
     * @param japaneseText Japanese word or phrase
     * @param userId User ID for quota tracking (null for anonymous)
     * @return Translation result
     * @throws QuotaExceededException if user exceeds free quota
     */
    VocabularyLookupResponse translateWithAi(String japaneseText, String userId);

    /**
     * Check remaining AI quota for user
     *
     * @param userId User ID
     * @return Remaining quota count (null = unlimited for Pro users)
     */
    Integer getRemainingQuota(String userId);

    /**
     * Check if user has Pro subscription (unlimited quota)
     *
     * @param userId User ID
     * @return true if Pro user
     */
    boolean isProUser(String userId);
}
