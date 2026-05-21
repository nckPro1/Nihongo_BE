package org.example.nihongobackend.service.vocabulary;

import org.example.nihongobackend.dto.response.vocabulary.QuickTranslateResponse;

public interface QuickTranslateService {

    QuickTranslateResponse translate(String rawText);
}
