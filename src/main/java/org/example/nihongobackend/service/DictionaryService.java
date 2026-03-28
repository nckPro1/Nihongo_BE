package org.example.nihongobackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nihongobackend.config.DeepSeekProperties;
import org.example.nihongobackend.service.vocabulary.VocabularyLookupLimits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DictionaryService {

    private static final Logger log = LoggerFactory.getLogger(DictionaryService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final DeepSeekProperties deepSeekProperties;

    public DictionaryService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            DeepSeekProperties deepSeekProperties
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.deepSeekProperties = deepSeekProperties;
    }

    /**
     * Việt → Nhật: DeepSeek trả JSON {@code japanese} (kanji/kana + đọc trong ngoặc khi cần).
     */
    public Map<String, String> searchVietToNhat(String vietnameseWord) {
        assertLookupLength(vietnameseWord);
        if (deepSeekProperties.getApiKey().isBlank()) {
            throw new IllegalStateException("Chưa cấu hình deepseek.api-key (DEEPSEEK_API_KEY)");
        }
        String system = """
                Bạn là từ điển Việt→Nhật gọn. Chỉ trả về MỘT object JSON hợp lệ, không markdown, không giải thích.
                Key bắt buộc: "japanese" — cách nói tự nhiên tiếng Nhật; với từ thường dùng kanji và thêm đọc hiragana trong ngoặc nếu khác bề mặt (vd 仕事 (しごと), 食べる).
                """;
        JsonNode root = callDeepSeekJson(system, vietnameseWord);
        String ja = textOrEmpty(root, "japanese");
        if (ja.isEmpty()) {
            throw new IllegalStateException("Model không trả trường japanese");
        }

        Map<String, String> out = new LinkedHashMap<>();
        out.put("keyword", vietnameseWord);
        out.put("ja", ja);
        out.put("hiragana", "");
        out.put("kanji", ja);
        return out;
    }

    /**
     * Nhật → Việt: DeepSeek trả {@code vietnamese} và tùy chọn {@code reading} (hiragana toàn cụm).
     */
    public Map<String, String> searchNhatToViet(String japaneseWord) {
        assertLookupLength(japaneseWord);
        if (deepSeekProperties.getApiKey().isBlank()) {
            throw new IllegalStateException("Chưa cấu hình deepseek.api-key (DEEPSEEK_API_KEY)");
        }
        String system = """
                Bạn là từ điển Nhật→Việt gọn. Chỉ trả về MỘT object JSON hợp lệ, không markdown, không giải thích.
                Keys: "vietnamese" (nghĩa tiếng Việt ngắn, tự nhiên), "reading" (toàn bộ cụm nhập bằng hiragana; nếu đầu vào đã là kana thì lặp lại).
                """;
        JsonNode root = callDeepSeekJson(system, japaneseWord);
        String meaningVi = textOrEmpty(root, "vietnamese");
        if (meaningVi.isEmpty()) {
            throw new IllegalStateException("Model không trả trường vietnamese");
        }
        String reading = textOrEmpty(root, "reading");
        String meaningFormatted = reading.isEmpty() ? meaningVi : meaningVi + " — " + reading;

        Map<String, String> out = new LinkedHashMap<>();
        out.put("keyword", japaneseWord);
        out.put("meaning", meaningVi);
        out.put("hiragana", reading);
        out.put("meaningFormatted", meaningFormatted);
        return out;
    }

    private static void assertLookupLength(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Nội dung tra cứu không được để trống");
        }
        if (text.length() > VocabularyLookupLimits.MAX_TEXT_CHARS) {
            throw new IllegalArgumentException(
                    "Tối đa " + VocabularyLookupLimits.MAX_TEXT_CHARS + " ký tự (chỉ hỗ trợ từ / cụm ngắn)"
            );
        }
    }

    private JsonNode callDeepSeekJson(String systemPrompt, String userText) {
        String base = deepSeekProperties.getBaseUrl();
        if (base.isBlank()) {
            throw new IllegalStateException("deepseek.base-url trống");
        }
        URI uri = URI.create(base + "/chat/completions");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekProperties.getApiKey());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", deepSeekProperties.getModel());
        body.put("temperature", 0.2);
        body.put("max_tokens", 512);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userText)
        ));

        log.info("[DeepSeek] dictionary request textLen={} model={}", userText.length(), deepSeekProperties.getModel());

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );
            String raw = response.getBody();
            if (raw == null || raw.isBlank()) {
                throw new IllegalStateException("Phản hồi DeepSeek trống");
            }
            JsonNode envelope = objectMapper.readTree(raw);
            if (envelope.has("error")) {
                String msg = envelope.path("error").path("message").asText("DeepSeek error");
                throw new IllegalStateException(msg);
            }
            JsonNode choices = envelope.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new IllegalStateException("Phản hồi DeepSeek không có choices[]");
            }
            String content = choices.get(0).path("message").path("content").asText("").trim();
            if (content.isEmpty()) {
                throw new IllegalStateException("Không có nội dung trong choices[0].message.content");
            }
            return parseModelJsonObject(content);
        } catch (RestClientException e) {
            log.warn("Gọi DeepSeek lỗi HTTP: {}", e.getMessage());
            throw new IllegalStateException("Không gọi được DeepSeek: " + e.getMessage());
        } catch (Exception e) {
            if (e instanceof IllegalStateException ise) {
                throw ise;
            }
            throw new IllegalStateException("Lỗi xử lý phản hồi DeepSeek: " + e.getMessage());
        }
    }

    private JsonNode parseModelJsonObject(String content) throws com.fasterxml.jackson.core.JsonProcessingException {
        String t = content.trim();
        if (t.startsWith("```")) {
            int nl = t.indexOf('\n');
            int end = t.lastIndexOf("```");
            if (nl > 0 && end > nl) {
                t = t.substring(nl + 1, end).trim();
            }
        }
        if (!t.startsWith("{")) {
            throw new IllegalStateException("Model không trả JSON object: " + t.substring(0, Math.min(80, t.length())));
        }
        return objectMapper.readTree(t);
    }

    private static String textOrEmpty(JsonNode node, String field) {
        if (node == null || !node.has(field)) {
            return "";
        }
        return node.get(field).asText("").trim();
    }
}
