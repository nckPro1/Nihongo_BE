package org.example.nihongobackend.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nihongobackend.config.ClaudeProperties;
import org.example.nihongobackend.dto.response.vocabulary.QuickTranslateResponse;
import org.example.nihongobackend.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import org.example.nihongobackend.service.vocabulary.VocabularyInputClassifier;

import java.text.Normalizer;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClaudeVocabularyService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeVocabularyService.class);

    private static final String ANTHROPIC_VERSION = "2023-06-01";
    /** JSON ngắn — giữ output nhỏ để tiết kiệm token (output đắt hơn input). */
    private static final int MAX_OUTPUT_TOKENS = 200;

    /**
     * Ép kiểu JSON tối thiểu — không chào hỏi, không giải thích ngoài object.
     */
    private static final String SYSTEM_JSON_ONLY =
            "Bạn là từ điển Nhật–Việt và Việt–Nhật.\n"
                    + "Trả về ĐÚNG một object JSON, không markdown, không ```, không chữ trước/sau JSON, "
                    + "KHÔNG chào hỏi, KHÔNG \"Chào bạn\", KHÔNG giải thích dài.\n"
                    + "Cấu trúc bắt buộc (đủ 3 key, string có thể rỗng \"\" nếu không áp dụng):\n"
                    + "{\"kanji\":\"\",\"romaji\":\"\",\"meaning\":\"\"}\n"
                    + "- kanji: từ/cụm tiếng Nhật (kanji + hiragana/katakana nếu cần). Romaji-only → điền bản kana/kanji tương ứng nếu biết, không thì \"\".\n"
                    + "- romaji: Hepburn chữ thường, không dấu (vd hikari, ganbaru).\n"
                    + "- meaning: một cụm ngắn tiếng Việt: nghĩa hoặc giải thích (≤180 ký tự).\n"
                    + "Hai chiều: nhập tiếng Nhật → meaning là nghĩa Việt. Nhập tiếng Việt → kanji+romaji là từ Nhật tương ứng, meaning là nghĩa Việt ngắn.";

    /**
     * Đầu vào xác định là tiếng Việt — ép bản Nhật đầy đủ, tránh chỉ trả meaning tiếng Việt.
     */
    private static final String SYSTEM_VI_JP =
            "Đầu vào là TIẾNG VIỆT. Bạn phải đưa cách nói tiếng Nhật tương ứng.\n"
                    + "Chỉ một JSON, không markdown, không ```, không chữ ngoài JSON.\n"
                    + "{\"kanji\":\"\",\"romaji\":\"\",\"meaning\":\"\"}\n"
                    + "- kanji: BẮT BUỘC cụm tiếng Nhật tự nhiên (kanji + hiragana/katakana). Ví dụ \"ăn cơm\" → ご飯を食べる. "
                    + "Không để trống nếu có cách diễn đạt thông dụng.\n"
                    + "- romaji: Hepburn chữ thường, đọc của cụm trong kanji (vd gohan o taberu).\n"
                    + "- meaning: một câu tiếng Việt rất ngắn (gợi ý ngữ cảnh hoặc nhắc nghĩa). "
                    + "KHÔNG được chỉ lặp lại giải thích dài trong khi kanji/romaji trống.";

    private final WebClient anthropicWebClient;
    private final ClaudeProperties properties;
    private final ObjectMapper objectMapper;

    public ClaudeVocabularyService(
            @Qualifier("anthropicWebClient") WebClient anthropicWebClient,
            ClaudeProperties properties,
            ObjectMapper objectMapper
    ) {
        this.anthropicWebClient = anthropicWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public QuickTranslateResponse quickTranslate(String rawText) {
        ensureApiKey();
        String text = rawText == null ? "" : rawText.trim();
        text = Normalizer.normalize(text, Normalizer.Form.NFC);
        if (text.isEmpty()) {
            throw new BadRequestException("Nội dung tra cứu không được để trống");
        }

        String model = properties.getVocabularyModel();
        if (model == null || model.isBlank()) {
            model = "claude-haiku-4-5-20251001";
        }

        boolean vi = VocabularyInputClassifier.looksLikeVietnamese(text);
        String system = vi ? SYSTEM_VI_JP : SYSTEM_JSON_ONLY;
        String userContent = vi ? "Việt→Nhật: " + text : "Tra: " + text;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("max_tokens", MAX_OUTPUT_TOKENS);
        body.put("system", system);
        body.put("messages", List.of(Map.of("role", "user", "content", userContent)));

        try {
            JsonNode root = anthropicWebClient.post()
                    .uri("/messages")
                    .header("x-api-key", properties.getApiKey())
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(45));

            if (root == null || !root.has("content")) {
                throw new BadRequestException("Phản hồi Claude không hợp lệ");
            }
            String assistantText = extractAssistantText(root);
            return parseJsonOrFallback(assistantText);
        } catch (WebClientResponseException e) {
            String errBody = "";
            try {
                errBody = e.getResponseBodyAsString();
            } catch (Exception ignored) {
                errBody = "(could not read body)";
            }
            log.warn("Anthropic API error: {} — {}", e.getStatusCode(), errBody);
            throw new BadRequestException("Không gọi được Claude: " + e.getStatusCode());
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Claude vocabulary failed", e);
            throw new BadRequestException("Lỗi dịch từ vựng: " + e.getMessage());
        }
    }

    private void ensureApiKey() {
        String key = properties.getApiKey();
        if (key == null || key.isBlank()) {
            throw new BadRequestException("Chưa cấu hình CLAUDE_API_KEY trên server.");
        }
    }

    private static String extractAssistantText(JsonNode root) {
        JsonNode content = root.get("content");
        if (content == null || !content.isArray() || content.isEmpty()) {
            return "";
        }
        JsonNode first = content.get(0);
        if (first != null && first.has("text")) {
            return first.get("text").asText("");
        }
        return "";
    }

    private QuickTranslateResponse parseJsonOrFallback(String assistantText) {
        String trimmed = assistantText == null ? "" : assistantText.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end > start) {
                trimmed = trimmed.substring(start, end + 1);
            }
        }
        try {
            return objectMapper.readValue(trimmed, QuickTranslateResponse.class);
        } catch (Exception e) {
            QuickTranslateResponse r = new QuickTranslateResponse();
            r.setKanji("");
            r.setRomaji("");
            r.setMeaning(trimmed);
            return r;
        }
    }
}
