package org.example.nihongobackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Claude / Anthropic. Tra từ vựng nhanh chỉ dùng {@link #vocabularyModel} (mặc định Haiku 3 để tiết kiệm).
 */
@ConfigurationProperties(prefix = "claude")
public class ClaudeProperties {

    private String apiKey = "";
    /** Model dùng cho các tính năng AI chung (sau này). */
    private String model = "claude-haiku-4-5-20251001";
    /**
     * Chỉ dùng cho dịch / tra từ vựng nhanh — Haiku (rẻ hơn Sonnet/Opus).
     */
    private String vocabularyModel = "claude-haiku-4-5-20251001";
    /**
     * Dịch câu / đoạn + hiragana + cụm kanji (Haiku 4.5).
     */
    private String passageModel = "claude-haiku-4-5-20251001";
    private String baseUrl = "https://api.anthropic.com/v1";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVocabularyModel() {
        return vocabularyModel;
    }

    public void setVocabularyModel(String vocabularyModel) {
        this.vocabularyModel = vocabularyModel;
    }

    public String getPassageModel() {
        return passageModel;
    }

    public void setPassageModel(String passageModel) {
        this.passageModel = passageModel;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
