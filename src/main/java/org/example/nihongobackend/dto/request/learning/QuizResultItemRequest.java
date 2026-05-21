package org.example.nihongobackend.dto.request.learning;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class QuizResultItemRequest {

    @NotNull(message = "flashcardId là bắt buộc")
    private UUID flashcardId;

    @NotNull(message = "correct là bắt buộc")
    private Boolean correct;

    public UUID getFlashcardId() {
        return flashcardId;
    }

    public void setFlashcardId(UUID flashcardId) {
        this.flashcardId = flashcardId;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }
}
