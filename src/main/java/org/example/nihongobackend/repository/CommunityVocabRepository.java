package org.example.nihongobackend.repository;

import org.example.nihongobackend.entity.CommunityVocab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Layer 2: Community Vietnamese-Japanese Dictionary (ODVP)
 */
public interface CommunityVocabRepository extends JpaRepository<CommunityVocab, UUID> {

    /**
     * Find by exact word match
     */
    Optional<CommunityVocab> findByWord(String word);

    /**
     * Find by exact reading match
     */
    Optional<CommunityVocab> findByReading(String reading);

    /**
     * Bidirectional search: word, reading, or Vietnamese meaning
     * Priority: word > reading > exact Vietnamese > partial Vietnamese
     *
     * Note: Using GIN trigram index for LIKE queries instead of B-tree
     * to avoid index size limitations on long Vietnamese meanings
     */
    @Query("""
        SELECT v FROM CommunityVocab v
        WHERE v.word = :text
           OR v.reading = :text
           OR LOWER(v.meaningVi) = LOWER(:text)
           OR LOWER(v.meaningVi) LIKE LOWER(CONCAT('%', :text, '%'))
        ORDER BY
            CASE
                WHEN v.word = :text THEN 0
                WHEN v.reading = :text THEN 1
                WHEN LOWER(v.meaningVi) = LOWER(:text) THEN 2
                WHEN LOWER(v.meaningVi) LIKE LOWER(CONCAT('%', :text, '%')) THEN 3
                ELSE 4
            END,
            v.priority DESC,
            v.searchCount DESC
        LIMIT 1
    """)
    Optional<CommunityVocab> findBestMatch(@Param("text") String text);

    /**
     * Find all by source (e.g., 'odvp', 'jmdict_vi')
     */
    List<CommunityVocab> findBySource(String source);

    /**
     * Find top N most searched words
     */
    @Query("""
        SELECT v FROM CommunityVocab v
        WHERE v.searchCount > 0
        ORDER BY v.searchCount DESC
        LIMIT :limit
    """)
    List<CommunityVocab> findTopSearched(@Param("limit") int limit);

    /**
     * Increment search count for a word
     */
    @Modifying
    @Query("""
        UPDATE CommunityVocab v
        SET v.searchCount = v.searchCount + 1,
            v.lastSearched = CURRENT_TIMESTAMP
        WHERE v.id = :id
    """)
    void incrementSearchCount(@Param("id") UUID id);

    /**
     * Count by source
     */
    long countBySource(String source);
}
