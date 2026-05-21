package org.example.nihongobackend.repository;

import org.example.nihongobackend.entity.FlashcardQuizProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlashcardQuizProgressRepository extends JpaRepository<FlashcardQuizProgress, UUID> {

    Optional<FlashcardQuizProgress> findByUser_IdAndFlashcard_Id(UUID userId, UUID flashcardId);

    List<FlashcardQuizProgress> findByUser_IdAndFlashcard_IdIn(UUID userId, Collection<UUID> flashcardIds);

    void deleteByFlashcard_Id(UUID flashcardId);

    void deleteByFlashcard_IdIn(Collection<UUID> flashcardIds);
}
