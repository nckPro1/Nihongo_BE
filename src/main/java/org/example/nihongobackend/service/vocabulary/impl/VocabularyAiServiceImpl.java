package org.example.nihongobackend.service.vocabulary.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nihongobackend.config.DeepSeekProperties;
import org.example.nihongobackend.dto.response.vocabulary.VocabularyLookupResponse;
import org.example.nihongobackend.entity.AiVocabCache;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.repository.AiVocabCacheRepository;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.service.vocabulary.QuotaExceededException;
import org.example.nihongobackend.service.vocabulary.VocabularyAiService;
import org.example.nihongobackend.service.vocabulary.VocabularyInputClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Layer 4 AI Service Implementation using DeepSeek
 * - Free tier: 50 AI lookups per day
 * - Pro tier: Unlimited lookups
 * - Auto-saves to Layer 3 global cache
 */
@Service
public class VocabularyAiServiceImpl implements VocabularyAiService {

    private static final Logger log = LoggerFactory.getLogger(VocabularyAiServiceImpl.class);
    private static final int FREE_DAILY_QUOTA = 50;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final DeepSeekProperties deepSeekProperties;
    private final AiVocabCacheRepository aiVocabCacheRepository;
    private final UserRepository userRepository;

    // In-memory quota tracking (TODO: Move to Redis for production)
    private final Map<String, QuotaTracker> quotaMap = new LinkedHashMap<>();

    public VocabularyAiServiceImpl(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            DeepSeekProperties deepSeekProperties,
            AiVocabCacheRepository aiVocabCacheRepository,
            UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.deepSeekProperties = deepSeekProperties;
        this.aiVocabCacheRepository = aiVocabCacheRepository;
        this.userRepository = userRepository;
    }

    @Override
    public VocabularyLookupResponse translateWithAi(String inputText, String userId) {
        long startTime = System.currentTimeMillis();

        // Check quota for free users
        if (!isProUser(userId)) {
            if (!checkAndDecrementQuota(userId)) {
                throw new QuotaExceededException(
                        "Daily AI lookup quota exceeded (50/day). Upgrade to Pro for unlimited access.");
            }
        }

        try {
            // Detect direction: Vietnamese → Japanese or Japanese → Vietnamese
            boolean isVietnameseInput = VocabularyInputClassifier.looksLikeVietnamese(inputText);

            log.info("[Vocab] L4 AI call: {} (direction: {}, user: {})",
                    inputText,
                    isVietnameseInput ? "vi→ja" : "ja→vi",
                    userId != null ? userId : "anonymous");

            String translationResult = callDeepSeekApi(inputText, isVietnameseInput);
            long latency = System.currentTimeMillis() - startTime;

            // Determine word (Japanese) and meaningVi (Vietnamese) based on direction
            String japaneseWord;
            String vietnameseMeaning;

            if (isVietnameseInput) {
                // Vi → Ja: input is Vietnamese, output is Japanese
                japaneseWord = translationResult;
                vietnameseMeaning = inputText;
            } else {
                // Ja → Vi: input is Japanese, output is Vietnamese
                japaneseWord = inputText;
                vietnameseMeaning = translationResult;
            }

            // Save to Layer 3 global cache (benefits ALL users)
            saveToCache(japaneseWord, vietnameseMeaning);

            return VocabularyLookupResponse.builder()
                    .success(true)
                    .query(inputText)
                    .word(japaneseWord)
                    .reading(null)  // DeepSeek doesn't provide reading
                    .meaningEn(null)
                    .meaningVi(vietnameseMeaning)
                    .level(null)
                    .partOfSpeech(null)
                    .source("ai_service")
                    .quality("ai_generated")
                    .latencyMs(latency)
                    .build();

        } catch (Exception e) {
            log.error("[Vocab] L4 AI call failed: {}", e.getMessage());
            throw new RuntimeException("AI translation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Integer getRemainingQuota(String userId) {
        if (isProUser(userId)) {
            return null;  // Unlimited
        }

        QuotaTracker tracker = getOrCreateQuotaTracker(userId);
        return tracker.remaining;
    }

    @Override
    public boolean isProUser(String userId) {
        if (userId == null) {
            return false;  // Anonymous users are free tier
        }

        try {
            User user = userRepository.findById(UUID.fromString(userId)).orElse(null);
            if (user == null) {
                return false;
            }

            // Check if user has active PREMIUM subscription
            return user.getRole().isPremiumOrAbove();

        } catch (Exception e) {
            log.warn("[Vocab] Failed to check Pro status for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Call DeepSeek API for bidirectional translation (Ja↔Vi)
     * @param inputText Input text (Japanese or Vietnamese)
     * @param isVietnameseInput true if input is Vietnamese (Vi→Ja), false if Japanese (Ja→Vi)
     * @return Translation result
     */
    private String callDeepSeekApi(String inputText, boolean isVietnameseInput) {
        try {
            String apiKey = deepSeekProperties.getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("DeepSeek API key not configured");
            }

            String url = deepSeekProperties.getBaseUrl() + "/chat/completions";

            // Prepare request
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", deepSeekProperties.getModel());

            Map<String, String> systemMessage = new LinkedHashMap<>();
            systemMessage.put("role", "system");

            // Different prompts based on direction
            if (isVietnameseInput) {
                // Vietnamese → Japanese
                systemMessage.put("content",
                        "Bạn là từ điển Việt-Nhật. Dịch từ/cụm tiếng Việt sang tiếng Nhật (kanji/kana) ngắn gọn (1-5 từ). " +
                                "Chỉ trả JSON: {\"japanese\":\"...\"}. Không giải thích, không markdown.");
            } else {
                // Japanese → Vietnamese
                systemMessage.put("content",
                        "Bạn là từ điển Nhật-Việt. Dịch từ/cụm tiếng Nhật sang tiếng Việt ngắn gọn (1-5 từ). " +
                                "Chỉ trả JSON: {\"vietnamese\":\"...\"}. Không giải thích, không markdown.");
            }

            Map<String, String> userMessage = new LinkedHashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", inputText);

            requestBody.put("messages", List.of(systemMessage, userMessage));
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 100);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            // Parse response
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0)
                    .path("message").path("content").asText();

            // Extract translation from JSON response based on direction
            JsonNode contentJson = objectMapper.readTree(content);
            String translation;

            if (isVietnameseInput) {
                translation = contentJson.path("japanese").asText();
            } else {
                translation = contentJson.path("vietnamese").asText();
            }

            if (translation.isBlank()) {
                throw new IllegalStateException("AI returned empty translation");
            }

            return translation;

        } catch (Exception e) {
            log.error("[Vocab] DeepSeek API call failed: {}", e.getMessage());
            throw new RuntimeException("AI API call failed", e);
        }
    }

    /**
     * Save translation result to Layer 3 cache
     */
    private void saveToCache(String word, String meaningVi) {
        try {
            AiVocabCache cache = new AiVocabCache();
            cache.setWord(word);
            cache.setMeaningVi(meaningVi);
            cache.setSource("ai");
            cache.setModel(deepSeekProperties.getModel());
            cache.setQueryCount(1);

            aiVocabCacheRepository.save(cache);

            log.info("[Vocab] Saved to L3 cache: {} → {}", word, meaningVi);

        } catch (DataIntegrityViolationException e) {
            // Race condition: another request already saved this word
            log.debug("[Vocab] Cache entry already exists: {}", word);
        } catch (Exception e) {
            log.warn("[Vocab] Failed to save to cache: {}", e.getMessage());
        }
    }

    /**
     * Check and decrement quota for user
     */
    private boolean checkAndDecrementQuota(String userId) {
        QuotaTracker tracker = getOrCreateQuotaTracker(userId);

        synchronized (tracker) {
            // Reset quota if new day
            if (!tracker.date.equals(LocalDate.now())) {
                tracker.remaining = FREE_DAILY_QUOTA;
                tracker.date = LocalDate.now();
            }

            if (tracker.remaining <= 0) {
                return false;
            }

            tracker.remaining--;
            return true;
        }
    }

    /**
     * Get or create quota tracker for user
     */
    private QuotaTracker getOrCreateQuotaTracker(String userId) {
        String key = userId != null ? userId : "anonymous";

        return quotaMap.computeIfAbsent(key, k -> {
            QuotaTracker tracker = new QuotaTracker();
            tracker.remaining = FREE_DAILY_QUOTA;
            tracker.date = LocalDate.now();
            return tracker;
        });
    }

    /**
     * Internal quota tracking
     */
    private static class QuotaTracker {
        int remaining;
        LocalDate date;
    }
}
