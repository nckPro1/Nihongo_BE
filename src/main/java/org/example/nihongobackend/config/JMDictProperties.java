package org.example.nihongobackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jmdict")
public class JMDictProperties {

    /** Đường dẫn tới file jmdict-all-x.x.x.json (để trống = bỏ qua import). */
    private String filePath = "";

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public boolean isConfigured() {
        return filePath != null && !filePath.isBlank();
    }
}
