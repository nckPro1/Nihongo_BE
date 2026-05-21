package org.example.nihongobackend.service.impl.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nihongobackend.config.ClaudeProperties;
import org.example.nihongobackend.dto.request.vocabulary.PassageTranslateRequest;
import org.example.nihongobackend.dto.response.vocabulary.PassageCompoundWord;
import org.example.nihongobackend.dto.response.vocabulary.PassageTranslateResponse;
import org.example.nihongobackend.exception.BadRequestException;
import org.example.nihongobackend.service.ai.ClaudePassageService;
import org.example.nihongobackend.service.vocabulary.VocabularyInputClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ClaudePassageServiceImpl implements ClaudePassageService {

    private static final Logger log = LoggerFactory.getLogger(ClaudePassageServiceImpl.class);

    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final int MAX_OUTPUT_TOKENS = 4096;

    private static final String SYSTEM_PASSAGE = """
            Bạn hỗ trợ học tiếng Nhật. Người dùng gửi một câu hoặc đoạn ngắn (tiếng Việt HOẶC tiếng Nhật).
            Phải trả về ĐÚNG một object JSON hợp lệ, không markdown, không ```, không chữ ngoài JSON.

            Cấu trúc bắt buộc:
            {
              "direction": "vi_jp" hoặc "jp_vi",
              "japanese": "toàn bộ câu tiếng Nhật (nếu đầu vào là Việt thì đây là bản dịch; nếu đầu vào là Nhật thì giữ nguyên câu đó)",
              "vietnamese": "toàn bộ câu tiếng Việt (nếu đầu vào là Việt thì giữ nguyên hoặc chỉnh nhẹ cho tự nhiên; nếu đầu vào là Nhật thì đây là bản dịch)",
              "hiraganaLine": "phiên âm toàn câu của chuỗi trong japanese — chỉ hiragana (katakana tên riêng có thể giữ katakana); không thêm dấu cách thừa giữa các mora trừ khi cần tách từ.",
              "compounds": [
                {"surface":"chuỗi con trùng khớp CHÍNH XÁC trong japanese","reading":"đọc hiragana","glossVi":"nghĩa ngắn tiếng Việt"}
              ]
            }

            Quy tắc compounds:
            - Chỉ liệt kê cụm có ít nhất một chữ kanji trong japanese (động từ có kanji, danh từ, 熟語…).
            - surface phải là substring liên tục của japanese, không được tự ý đổi ký tự.
            - Ưu tiên cụm dài hơn khi có chồng lấn (vd 大学生 trước 大).
            - glossVi ngắn (≤ 100 ký tự).
            - Nếu không có kanji nào đáng chú ý, compounds có thể là mảng rỗng [].
            """;

    private final WebClient anthropicWebClient;
    private final ClaudeProperties properties;
    private final ObjectMapper objectMapper;

    public ClaudePassageServiceImpl(
            @Qualifier("anthropicWebClient") WebClient anthropicWebClient,
            ClaudeProperties properties,
            ObjectMapper objectMapper
    ) {
        this.anthropicWebClient = anthropicWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public PassageTranslateResponse translatePassage(String rawText) {
        String key = properties.getApiKey();
        if (key == null || key.isBlank()) {
            throw new BadRequestException("Chưa cấu hình CLAUDE_API_KEY trên server.");
        }

        String text = rawText == null ? "" : rawText.trim();
        text = Normalizer.normalize(text, Normalizer.Form.NFC);
        if (text.isEmpty()) {
            throw new BadRequestException("Nội dung không được để trống");
        }
        if (text.length() > PassageTranslateRequest.MAX_CHARS) {
            throw new BadRequestException("Tối đa " + PassageTranslateRequest.MAX_CHARS + " ký tự");
        }

        String model = properties.getPassageModel();
        if (model == null || model.isBlank()) {
            model = "claude-haiku-4-5-20251001";
        }

        boolean vi = VocabularyInputClassifier.looksLikeVietnamese(text);
        String hint = vi
                ? "Đầu vào tiếng Việt — dịch sang tiếng Nhật tự nhiên và điền JSON theo hướng vi_jp."
                : "Đầu vào tiếng Nhật — dịch sang tiếng Việt và điền JSON theo hướng jp_vi.";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("max_tokens", MAX_OUTPUT_TOKENS);
        body.put("system", SYSTEM_PASSAGE);
        body.put("messages", List.of(Map.of("role", "user", "content", hint + "\n\n---\n" + text)));

        try {
            JsonNode root = anthropicWebClient.post()
                    .uri("/messages")
                    .header("x-api-key", properties.getApiKey())
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(120));

            if (root == null || !root.has("content")) {
                throw new BadRequestException("Phản hồi Claude không hợp lệ");
            }
            String assistantText = extractAssistantText(root);
            return parsePassageJson(assistantText);
        } catch (WebClientResponseException e) {
            String errBody = "";
            try {
                errBody = e.getResponseBodyAsString();
            } catch (Exception ignored) {
                errBody = "";
            }
            log.warn("Anthropic passage API error: {} — {}", e.getStatusCode(), errBody);
            throw new BadRequestException("Không gọi được Claude: " + e.getStatusCode());
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Claude passage failed", e);
            throw new BadRequestException("Lỗi dịch đoạn: " + e.getMessage());
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

    private PassageTranslateResponse parsePassageJson(String assistantText) {
        String trimmed = assistantText == null ? "" : assistantText.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end > start) {
                trimmed = trimmed.substring(start, end + 1);
            }
        }
        try {
            JsonNode n = objectMapper.readTree(trimmed);
            PassageTranslateResponse r = new PassageTranslateResponse();
            r.setDirection(n.path("direction").asText("").trim());
            r.setJapanese(n.path("japanese").asText("").trim());
            r.setVietnamese(n.path("vietnamese").asText("").trim());
            r.setHiraganaLine(n.path("hiraganaLine").asText("").trim());

            if (r.getJapanese().isEmpty()) {
                throw new BadRequestException("Model không trả trường japanese hợp lệ");
            }

            List<PassageCompoundWord> out = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            JsonNode arr = n.path("compounds");
            if (arr.isArray()) {
                for (JsonNode c : arr) {
                    String surface = c.path("surface").asText("").trim();
                    if (surface.isEmpty() || !r.getJapanese().contains(surface) || !containsKanji(surface)) {
                        continue;
                    }
                    String dedupKey = surface + "\0" + c.path("reading").asText("");
                    if (!seen.add(dedupKey)) {
                        continue;
                    }
                    String gloss = c.path("glossVi").asText("").trim();
                    if (gloss.isEmpty()) {
                        gloss = c.path("gloss").asText("").trim();
                    }
                    out.add(new PassageCompoundWord(
                            surface,
                            c.path("reading").asText("").trim(),
                            gloss
                    ));
                }
            }
            out.sort(Comparator.comparingInt((PassageCompoundWord w) -> w.getSurface().length()).reversed());
            r.setCompounds(out);
            return r;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Parse passage JSON failed: {}", e.toString());
            throw new BadRequestException("Không đọc được JSON từ Claude: " + e.getMessage());
        }
    }

    private static boolean containsKanji(String s) {
        return s.codePoints().anyMatch(cp -> (cp >= 0x4E00 && cp <= 0x9FFF) || (cp >= 0x3400 && cp <= 0x4DBF));
    }
}
