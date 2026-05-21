package org.example.nihongobackend.dto.response.vocabulary;

/**
 * Vocabulary lookup response with source metadata
 */
public class VocabularyLookupResponse {

    private boolean success;
    private String query;
    private String word;
    private String reading;
    private String meaningEn;
    private String meaningVi;
    private String level; // JLPT level (N5, N4, N3, N2, N1)
    private String partOfSpeech;
    private String source; // "own_vocab", "community_dict", "ai_cache", "ai_fresh"
    private String quality; // "verified", "unverified", "ai_generated"
    private Long latencyMs;

    // Constructors
    public VocabularyLookupResponse() {
    }

    public VocabularyLookupResponse(boolean success, String message) {
        this.success = success;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final VocabularyLookupResponse response = new VocabularyLookupResponse();

        public Builder success(boolean success) {
            response.success = success;
            return this;
        }

        public Builder query(String query) {
            response.query = query;
            return this;
        }

        public Builder word(String word) {
            response.word = word;
            return this;
        }

        public Builder reading(String reading) {
            response.reading = reading;
            return this;
        }

        public Builder meaningEn(String meaningEn) {
            response.meaningEn = meaningEn;
            return this;
        }

        public Builder meaningVi(String meaningVi) {
            response.meaningVi = meaningVi;
            return this;
        }

        public Builder level(String level) {
            response.level = level;
            return this;
        }

        public Builder partOfSpeech(String partOfSpeech) {
            response.partOfSpeech = partOfSpeech;
            return this;
        }

        public Builder source(String source) {
            response.source = source;
            return this;
        }

        public Builder quality(String quality) {
            response.quality = quality;
            return this;
        }

        public Builder latencyMs(Long latencyMs) {
            response.latencyMs = latencyMs;
            return this;
        }

        public VocabularyLookupResponse build() {
            return response;
        }
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getReading() {
        return reading;
    }

    public void setReading(String reading) {
        this.reading = reading;
    }

    public String getMeaningEn() {
        return meaningEn;
    }

    public void setMeaningEn(String meaningEn) {
        this.meaningEn = meaningEn;
    }

    public String getMeaningVi() {
        return meaningVi;
    }

    public void setMeaningVi(String meaningVi) {
        this.meaningVi = meaningVi;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }
}
