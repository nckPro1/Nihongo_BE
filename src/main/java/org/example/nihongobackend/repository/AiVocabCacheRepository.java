package org.example.nihongobackend.repository;

import org.example.nihongobackend.entity.AiVocabCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Layer 3: Shared AI Vocabulary Cache
 * - Global cache that grows over time
 * - Benefits ALL users (not per-user)
 */
public interface AiVocabCacheRepository extends JpaRepository<AiVocabCache, UUID> {

    /**
     * Find by exact word match (primary lookup for Layer 3)
     */
    Optional<AiVocabCache> findByWord(String word);

    /**
     * Find by word or reading (bidirectional support)
     */
    @Query("""
        SELECT v FROM AiVocabCache v
        WHERE v.word = :text
           OR v.reading = :text
           OR LOWER(v.meaningVi) = LOWER(:text)
           OR LOWER(v.meaningVi) LIKE LOWER(CONCAT('%', :text, '%'))
        ORDER BY
            CASE
                WHEN v.word = :text THEN 0
                WHEN v.reading = :text THEN 1
                WHEN LOWER(v.meaningVi) = LOWER(:text) THEN 2
                ELSE 3
            END,
            v.queryCount DESC
        LIMIT 1
    """)
    Optional<AiVocabCache> findBestMatch(@Param("text") String text);

    /**
     * Find top N most queried words (cache effectiveness tracking)
     */
    @Query("""
        SELECT v FROM AiVocabCache v
        ORDER BY v.queryCount DESC
        LIMIT :limit
    """)
    List<AiVocabCache> findTopCached(@Param("limit") int limit);

    /**
     * Find words with most reports (quality issues)
     */
    @Query("""
        SELECT v FROM AiVocabCache v
        WHERE v.reportCount > 0
        ORDER BY v.reportCount DESC
        LIMIT :limit
    """)
    List<AiVocabCache> findMostReported(@Param("limit") int limit);

    /**
     * Increment query count when cache hit
     */
    @Modifying
    @Query("""
        UPDATE AiVocabCache v
        SET v.queryCount = v.queryCount + 1,
            v.lastQueried = CURRENT_TIMESTAMP
        WHERE v.id = :id
    """)
    void incrementQueryCount(@Param("id") UUID id);

    /**
     * Count verified vs unverified
     */
    long countByIsVerified(Boolean isVerified);

    /**
     * Count by AI model
     */
    long countByModel(String model);
}
