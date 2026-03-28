package org.example.nihongobackend.service.vocabulary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickTranslateServiceTest {

    @Test
    void looksLikeVietnamese_detectsMarkedVietnamese() {
        assertTrue(VocabularyInputClassifier.looksLikeVietnamese("ăn cơm"));
        assertTrue(VocabularyInputClassifier.looksLikeVietnamese("nhật bản"));
        assertTrue(VocabularyInputClassifier.looksLikeVietnamese("công việc"));
        assertFalse(VocabularyInputClassifier.looksLikeVietnamese("頑張る"));
    }
}
