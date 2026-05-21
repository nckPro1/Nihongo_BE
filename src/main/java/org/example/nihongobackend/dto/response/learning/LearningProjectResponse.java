package org.example.nihongobackend.dto.response.learning;

import java.time.LocalDateTime;
import java.util.UUID;

public class LearningProjectResponse {

    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private long cardCount;
    /** Trung bình điểm nắm (0–100) trên toàn bộ thẻ trong Zenigo; thẻ chưa quiz = 0. */
    private int progressPercent;
    /** Số thẻ có mastery ≥ 80. */
    private long masteredCardCount;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getCardCount() {
        return cardCount;
    }

    public void setCardCount(long cardCount) {
        this.cardCount = cardCount;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public long getMasteredCardCount() {
        return masteredCardCount;
    }

    public void setMasteredCardCount(long masteredCardCount) {
        this.masteredCardCount = masteredCardCount;
    }
}
