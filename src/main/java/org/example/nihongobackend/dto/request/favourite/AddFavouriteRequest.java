package org.example.nihongobackend.dto.request.favourite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AddFavouriteRequest {

    @NotBlank(message = "Thiếu targetType")
    private String targetType;

    @NotNull(message = "Thiếu targetId")
    private UUID targetId;

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }
}
