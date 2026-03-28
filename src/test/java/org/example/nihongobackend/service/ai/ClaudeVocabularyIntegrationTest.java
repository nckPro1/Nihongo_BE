package org.example.nihongobackend.service.ai;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.nihongobackend.dto.response.vocabulary.QuickTranslateResponse;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Gọi thật Anthropic (Claude 3 Haiku). Chạy từ nihongo-backend khi .env có CLAUDE_API_KEY.
 * <pre>mvn test -Dtest=ClaudeVocabularyIntegrationTest</pre>
 */
@SpringBootTest
class ClaudeVocabularyIntegrationTest {

    @DynamicPropertySource
    static void loadClaudeFromEnv(DynamicPropertyRegistry registry) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        registry.add("claude.api-key", () -> dotenv.get("CLAUDE_API_KEY", ""));
        registry.add("claude.vocabulary-model", () -> dotenv.get("CLAUDE_VOCABULARY_MODEL", "claude-haiku-4-5-20251001"));
        registry.add("claude.base-url", () -> dotenv.get("CLAUDE_BASE_URL", "https://api.anthropic.com/v1"));
    }

    @Autowired
    private ClaudeVocabularyService claudeVocabularyService;

    @Test
    void quickTranslate_returnsMeaningForJapaneseWord() {
        String key = Dotenv.configure().ignoreIfMissing().load().get("CLAUDE_API_KEY", "");
        Assumptions.assumeFalse(key.isBlank(), "Bỏ qua: thêm CLAUDE_API_KEY vào .env để chạy test tích hợp");

        QuickTranslateResponse r = claudeVocabularyService.quickTranslate("頑張る");

        assertNotNull(r);
        assertNotNull(r.getMeaning());
        assertFalse(r.getMeaning().isBlank(), "meaning should not be empty");
    }
}
