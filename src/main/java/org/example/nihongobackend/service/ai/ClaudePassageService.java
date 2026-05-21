package org.example.nihongobackend.service.ai;

import org.example.nihongobackend.dto.response.vocabulary.PassageTranslateResponse;

public interface ClaudePassageService {

    PassageTranslateResponse translatePassage(String rawText);
}
