package org.example.nihongobackend.controller.flashcard;

import jakarta.validation.Valid;
import org.example.nihongobackend.dto.request.flashcard.BulkCreateFlashcardsRequest;
import org.example.nihongobackend.dto.request.flashcard.CreateFlashcardRequest;
import org.example.nihongobackend.dto.response.common.ApiResponse;
import org.example.nihongobackend.dto.response.flashcard.BulkCreateFlashcardsResponse;
import org.example.nihongobackend.dto.response.flashcard.FlashcardResponse;
import org.example.nihongobackend.service.flashcard.FlashcardService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/flashcards")
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @GetMapping
    public ApiResponse<List<FlashcardResponse>> list(@RequestParam("projectId") UUID projectId) {
        return ApiResponse.success("OK", flashcardService.listByProject(projectId));
    }

    @PostMapping
    public ApiResponse<FlashcardResponse> create(@Valid @RequestBody CreateFlashcardRequest request) {
        FlashcardResponse data = flashcardService.create(request);
        return ApiResponse.success("Đã thêm thẻ vào Zenigo", data);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        flashcardService.delete(id);
        return ApiResponse.success("Đã xoá thẻ", null);
    }

    @PostMapping("/batch")
    public ApiResponse<BulkCreateFlashcardsResponse> createBatch(@Valid @RequestBody BulkCreateFlashcardsRequest request) {
        BulkCreateFlashcardsResponse data = flashcardService.createBatch(request);
        StringBuilder msg = new StringBuilder();
        msg.append("Đã thêm ").append(data.getCreated()).append(" thẻ");
        if (data.getSkippedDuplicates() > 0) {
            msg.append(", bỏ qua ").append(data.getSkippedDuplicates()).append(" trùng");
        }
        if (data.getSkippedInvalid() > 0) {
            msg.append(", ").append(data.getSkippedInvalid()).append(" dòng không hợp lệ");
        }
        return ApiResponse.success(msg.toString(), data);
    }
}
