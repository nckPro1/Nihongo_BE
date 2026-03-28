package org.example.nihongobackend.dto.response.learning;

import java.time.LocalDateTime;
import java.util.UUID;

public class LearningProjectResponse {

    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private long cardCount;

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
}
