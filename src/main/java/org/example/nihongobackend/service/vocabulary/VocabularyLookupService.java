package org.example.nihongobackend.service.vocabulary;

import org.example.nihongobackend.dto.response.vocabulary.VocabularyLookupResponse;

import java.util.Map;

/**
 * Unified vocabulary lookup service implementing 4-layer strategy:
 * Layer 1: Own curated JLPT vocab (7,895 words, ~60% coverage, <10ms, $0)
 * Layer 2: Community dictionary (30k words, +25% coverage, <20ms, $0)
 * Layer 3: Shared AI cache (global, grows over time, <30ms, $0)
 * Layer 4: AI service (DeepSeek/Gemini, <1% queries, 500-1000ms, $0.0001)
 */
public interface VocabularyLookupService {

    /**
     * Lookup vocabulary with 4-layer fallback strategy
     * @param query Japanese word (kanji, kana) or reading
     * @param userId User ID for quota tracking (null for anonymous)
     * @return Vocabulary lookup result with source metadata
     */
    VocabularyLookupResponse lookup(String query, String userId);

    /**
     * Get vocabulary statistics
     */
    VocabularyStatsResponse getStats();

    /**
     * DEBUG: Search for a word in all layers and return debug info
     */
    Map<String, Object> debugSearch(String word);

    /**
     * DEBUG: Clear cache entry for a specific word
     */
    void clearCacheEntry(String word);
}
