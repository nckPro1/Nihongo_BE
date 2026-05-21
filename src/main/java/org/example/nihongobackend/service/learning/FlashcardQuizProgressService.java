package org.example.nihongobackend.service.learning;

import org.example.nihongobackend.dto.request.learning.QuizResultItemRequest;
import org.example.nihongobackend.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FlashcardQuizProgressService {

    int MASTERED_THRESHOLD = 80;

    record ProjectDeckProgress(int progressPercent, long masteredCardCount, long totalCards) {}

    ProjectDeckProgress computeDeckProgress(User user, UUID projectId);

    Map<UUID, Integer> masteryByFlashcardIds(User user, List<UUID> flashcardIds);

    void submitQuizResults(User user, UUID projectId, List<QuizResultItemRequest> results);

    void deleteProgressByFlashcardId(UUID flashcardId);

    void deleteProgressByFlashcardIds(Collection<UUID> flashcardIds);
}
