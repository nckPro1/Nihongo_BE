package org.example.nihongobackend.dto.request.vocabulary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PassageTranslateRequest {

    public static final int MAX_CHARS = 1000;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = MAX_CHARS, message = "Tối đa 1000 ký tự")
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
