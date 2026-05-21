package org.example.nihongobackend.service.vocabulary;

import java.util.Map;

/**
 * Vocabulary statistics response
 */
public class VocabularyStatsResponse {

    private long totalWords;
    private Map<String, Long> byLevel; // N5: 684, N4: 650, etc.
    private long verifiedWords;
    private long totalSearches;
    private Map<String, Long> bySource; // own_vocab: 7895, community: 0, etc.

    // Constructors
    public VocabularyStatsResponse() {
    }

    public VocabularyStatsResponse(long totalWords, Map<String, Long> byLevel, long verifiedWords, long totalSearches, Map<String, Long> bySource) {
        this.totalWords = totalWords;
        this.byLevel = byLevel;
        this.verifiedWords = verifiedWords;
        this.totalSearches = totalSearches;
        this.bySource = bySource;
    }

    // Getters and Setters
    public long getTotalWords() {
        return totalWords;
    }

    public void setTotalWords(long totalWords) {
        this.totalWords = totalWords;
    }

    public Map<String, Long> getByLevel() {
        return byLevel;
    }

    public void setByLevel(Map<String, Long> byLevel) {
        this.byLevel = byLevel;
    }

    public long getVerifiedWords() {
        return verifiedWords;
    }

    public void setVerifiedWords(long verifiedWords) {
        this.verifiedWords = verifiedWords;
    }

    public long getTotalSearches() {
        return totalSearches;
    }

    public void setTotalSearches(long totalSearches) {
        this.totalSearches = totalSearches;
    }

    public Map<String, Long> getBySource() {
        return bySource;
    }

    public void setBySource(Map<String, Long> bySource) {
        this.bySource = bySource;
    }
}
