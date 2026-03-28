package org.example.nihongobackend.controller.flashcard;

import jakarta.validation.Valid;
import org.example.nihongobackend.dto.request.flashcard.CreateFlashcardRequest;
import org.example.nihongobackend.dto.response.common.ApiResponse;
import org.example.nihongobackend.dto.response.flashcard.FlashcardResponse;
import org.example.nihongobackend.service.flashcard.FlashcardService;
import org.springframework.web.bind.annotation.GetMapping;
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
        return ApiResponse.success("Đã thêm thẻ vào dự án", data);
    }
}
