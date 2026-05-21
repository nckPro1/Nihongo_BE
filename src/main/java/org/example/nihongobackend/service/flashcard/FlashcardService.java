package org.example.nihongobackend.service.flashcard;

import org.example.nihongobackend.dto.request.flashcard.BulkCreateFlashcardsRequest;
import org.example.nihongobackend.dto.request.flashcard.CreateFlashcardRequest;
import org.example.nihongobackend.dto.response.flashcard.BulkCreateFlashcardsResponse;
import org.example.nihongobackend.dto.response.flashcard.FlashcardResponse;

import java.util.List;
import java.util.UUID;

public interface FlashcardService {

    List<FlashcardResponse> listByProject(UUID projectId);

    FlashcardResponse create(CreateFlashcardRequest request);

    BulkCreateFlashcardsResponse createBatch(BulkCreateFlashcardsRequest request);

    void delete(UUID flashcardId);
}
