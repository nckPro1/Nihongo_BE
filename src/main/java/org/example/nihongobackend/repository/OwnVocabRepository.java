package org.example.nihongobackend.repository;

import org.example.nihongobackend.entity.OwnVocab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Layer 1: Own curated JLPT vocabulary
 */
public interface OwnVocabRepository extends JpaRepository<OwnVocab, UUID> {

    /**
     * Find by exact word match
     */
    Optional<OwnVocab> findByWord(String word);

    /**
     * Find by exact reading match
     */
    Optional<OwnVocab> findByReading(String reading);

    /**
     * Find by word, reading, Vietnamese or English meaning (bidirectional)
     * Priority: word > reading > exact Vietnamese > exact English > partial Vietnamese > partial English
     */
    @Query("""
        SELECT v FROM OwnVocab v
        WHERE v.word = :text
           OR v.reading = :text
           OR LOWER(v.meaningVi) = LOWER(:text)
           OR LOWER(v.meaningEn) = LOWER(:text)
           OR LOWER(v.meaningVi) LIKE LOWER(CONCAT('%', :text, '%'))
           OR LOWER(v.meaningEn) LIKE LOWER(CONCAT('%', :text, '%'))
        ORDER BY
            CASE
                WHEN v.word = :text THEN 0
                WHEN v.reading = :text THEN 1
                WHEN LOWER(v.meaningVi) = LOWER(:text) THEN 2
                WHEN LOWER(v.meaningEn) = LOWER(:text) THEN 3
                WHEN LOWER(v.meaningVi) LIKE LOWER(CONCAT('%', :text, '%')) THEN 4
                WHEN LOWER(v.meaningEn) LIKE LOWER(CONCAT('%', :text, '%')) THEN 5
                ELSE 6
            END,
            v.searchCount DESC
        LIMIT 1
    """)
    Optional<OwnVocab> findBestMatch(@Param("text") String text);

    /**
     * Find all by JLPT level
     */
    List<OwnVocab> findByLevel(String level);

    /**
     * Find top N most searched words
     */
    @Query("""
        SELECT v FROM OwnVocab v
        WHERE v.searchCount > 0
        ORDER BY v.searchCount DESC
        LIMIT :limit
    """)
    List<OwnVocab> findTopSearched(@Param("limit") int limit);

    /**
     * Increment search count for a word
     */
    @Modifying
    @Query("""
        UPDATE OwnVocab v
        SET v.searchCount = v.searchCount + 1,
            v.lastSearched = CURRENT_TIMESTAMP
        WHERE v.id = :id
    """)
    void incrementSearchCount(@Param("id") UUID id);

    /**
     * Count by JLPT level
     */
    long countByLevel(String level);

    /**
     * Count verified words
     */
    long countByIsVerified(Boolean isVerified);
}
