package org.example.nihongobackend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(ClaudeProperties.class)
public class ClaudeConfig {

    @Bean
    public WebClient anthropicWebClient(WebClient.Builder builder, ClaudeProperties properties) {
        String base = properties.getBaseUrl() == null ? "" : properties.getBaseUrl().replaceAll("/+$", "");
        return builder.baseUrl(base).build();
    }
}
