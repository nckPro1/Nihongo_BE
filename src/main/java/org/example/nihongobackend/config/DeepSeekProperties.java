package org.example.nihongobackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {

    /** Không commit key thật — dùng DEEPSEEK_API_KEY trong môi trường hoặc .env */
    private String apiKey = "";

    private String baseUrl = "https://api.deepseek.com";

    private String model = "deepseek-chat";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model == null ? "deepseek-chat" : model.trim();
    }
}
