package org.example.nihongobackend.dto.request.flashcard;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class BulkCreateFlashcardsRequest {

    @NotNull(message = "Thiếu projectId")
    private UUID projectId;

    @NotEmpty(message = "Danh sách thẻ không được rỗng")
    @Size(max = 300, message = "Tối đa 300 thẻ mỗi lần")
    @Valid
    private List<BulkFlashcardItemRequest> items;

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public List<BulkFlashcardItemRequest> getItems() {
        return items;
    }

    public void setItems(List<BulkFlashcardItemRequest> items) {
        this.items = items;
    }
}
