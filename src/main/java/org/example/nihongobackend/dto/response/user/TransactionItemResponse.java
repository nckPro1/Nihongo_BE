package org.example.nihongobackend.dto.response.user;

import java.time.Instant;
import java.util.UUID;

/** Một dòng lịch sử giao dịch (PayOS / Pro — sẽ nối sau). */
public class TransactionItemResponse {
    private UUID id;
    private String description;
    private String amount;
    private String status;
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
