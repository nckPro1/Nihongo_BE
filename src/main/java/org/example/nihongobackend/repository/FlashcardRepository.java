package org.example.nihongobackend.repository;

import org.example.nihongobackend.entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FlashcardRepository extends JpaRepository<Flashcard, UUID> {

    boolean existsByProject_IdAndKanjiAndMeaning(UUID projectId, String kanji, String meaning);

    List<Flashcard> findByProject_IdOrderByCreatedAtDesc(UUID projectId);

    List<Flashcard> findByProjectIsNull();

    long countByProject_Id(UUID projectId);
}
