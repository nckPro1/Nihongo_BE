package org.example.nihongobackend.dto.request.vocabulary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.nihongobackend.service.vocabulary.VocabularyLookupLimits;

public class QuickTranslateRequest {

    @NotBlank(message = "Nội dung tra cứu không được để trống")
    @Size(max = VocabularyLookupLimits.MAX_TEXT_CHARS, message = "Tối đa 100 ký tự (từ / cụm ngắn)")
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
