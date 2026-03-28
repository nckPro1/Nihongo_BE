package org.example.nihongobackend.controller.vocabulary;

import jakarta.validation.Valid;
import org.example.nihongobackend.dto.request.vocabulary.PassageTranslateRequest;
import org.example.nihongobackend.dto.request.vocabulary.QuickTranslateRequest;
import org.example.nihongobackend.dto.response.common.ApiResponse;
import org.example.nihongobackend.dto.response.vocabulary.PassageTranslateResponse;
import org.example.nihongobackend.dto.response.vocabulary.QuickTranslateResponse;
import org.example.nihongobackend.service.ai.ClaudePassageService;
import org.example.nihongobackend.service.vocabulary.QuickTranslateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vocabulary")
public class VocabularyController {

    private final QuickTranslateService quickTranslateService;
    private final ClaudePassageService claudePassageService;

    public VocabularyController(
            QuickTranslateService quickTranslateService,
            ClaudePassageService claudePassageService
    ) {
        this.quickTranslateService = quickTranslateService;
        this.claudePassageService = claudePassageService;
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
}
