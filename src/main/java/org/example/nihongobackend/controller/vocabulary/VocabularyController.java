package org.example.nihongobackend.controller.vocabulary;

import jakarta.validation.Valid;
import org.example.nihongobackend.dto.request.vocabulary.PassageTranslateRequest;
import org.example.nihongobackend.dto.request.vocabulary.QuickTranslateRequest;
import org.example.nihongobackend.dto.response.common.ApiResponse;
import org.example.nihongobackend.dto.response.vocabulary.PassageTranslateResponse;
import org.example.nihongobackend.dto.response.vocabulary.QuickTranslateResponse;
import org.example.nihongobackend.dto.response.vocabulary.VocabularyLookupResponse;
import org.example.nihongobackend.service.ai.ClaudePassageService;
import org.example.nihongobackend.service.vocabulary.QuickTranslateService;
import org.example.nihongobackend.service.vocabulary.VocabularyLookupService;
import org.example.nihongobackend.service.vocabulary.VocabularyStatsResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/vocabulary")
public class VocabularyController {

    private final QuickTranslateService quickTranslateService;
    private final ClaudePassageService claudePassageService;
    private final VocabularyLookupService vocabularyLookupService;

    public VocabularyController(
            QuickTranslateService quickTranslateService,
            ClaudePassageService claudePassageService,
            VocabularyLookupService vocabularyLookupService
    ) {
        this.quickTranslateService = quickTranslateService;
        this.claudePassageService = claudePassageService;
        this.vocabularyLookupService = vocabularyLookupService;
    }

    /**
     * Tra từ nhanh — DeepSeek, hai chiều Vi↔Ja.
     */
    @PostMapping("/quick-translate")
    public ApiResponse<QuickTranslateResponse> quickTranslate(@Valid @RequestBody QuickTranslateRequest request) {
        QuickTranslateResponse data = quickTranslateService.translate(request.getText());
        return ApiResponse.success("OK", data);
    }

    /**
     * Dịch câu / đoạn — Claude Haiku 4.5: bản dịch nguyên câu, hiragana cả câu, cụm kanji (hover).
     */
    @PostMapping("/passage-translate")
    public ApiResponse<PassageTranslateResponse> passageTranslate(@Valid @RequestBody PassageTranslateRequest request) {
        PassageTranslateResponse data = claudePassageService.translatePassage(request.getText());
        return ApiResponse.success("OK", data);
    }

    /**
     * Lookup vocabulary với 4-layer strategy
     * Layer 1: Own JLPT vocab (7,895 words, <10ms, $0)
     * GET /api/vocabulary/lookup?query=食べる
     */
    @GetMapping("/lookup")
    public ApiResponse<?> lookup(
            @RequestParam("query") String query,
            Authentication authentication) {
        try {
            if (query == null || query.isBlank()) {
                return ApiResponse.error("Query parameter cannot be empty");
            }

            String userId = authentication != null ? authentication.getName() : null;
            VocabularyLookupResponse response = vocabularyLookupService.lookup(query.trim(), userId);

            if (response.isSuccess()) {
                return ApiResponse.success("Found", response);
            } else {
                return ApiResponse.error("Word not found in database");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage() != null ? e.getMessage() : "Lookup failed");
        }
    }

    /**
     * Get vocabulary statistics
     * GET /api/vocabulary/stats
     */
    @GetMapping("/stats")
    public ApiResponse<VocabularyStatsResponse> getStats() {
        try {
            VocabularyStatsResponse stats = vocabularyLookupService.getStats();
            return ApiResponse.success("OK", stats);
        } catch (Exception e) {
            return ApiResponse.error("Failed to get stats");
        }
    }

    /**
     * DEBUG endpoint - check database for specific word
     * GET /api/vocabulary/debug?word=Ăn
     */
    @GetMapping("/debug")
    public ApiResponse<?> debug(@RequestParam("word") String word) {
        try {
            Map<String, Object> debugInfo = vocabularyLookupService.debugSearch(word);
            return ApiResponse.success("Debug info", debugInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * DEBUG endpoint - clear cache entry
     * DELETE /api/vocabulary/cache?word=con%20chó
     */
    @DeleteMapping("/cache")
    public ApiResponse<?> clearCache(@RequestParam("word") String word) {
        try {
            vocabularyLookupService.clearCacheEntry(word);
            return ApiResponse.success("Cache cleared for: " + word, null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
