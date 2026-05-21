package org.example.nihongobackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Layer 2: Community Vietnamese-Japanese Dictionary (ODVP)
 * - 284k entries from yomichan-Vietnamese-dictionary
 * - Vietnamese translations only (no English)
 * - Fallback after own_vocab Layer 1 misses
 */
@Entity
@Table(name = "community_vocab")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityVocab {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Japanese word (kanji, hiragana, or katakana)
     */
    @Column(nullable = false, length = 500)
    private String word;

    /**
     * Reading in hiragana/katakana
     */
    @Column(length = 500)
    private String reading;

    /**
     * Vietnamese meaning
     * Note: ODVP dictionary only has Vietnamese, no English
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaningVi;

    /**
     * Part of speech (noun, verb, adjective, etc.)
     */
    @Column(length = 50)
    private String partOfSpeech;

    /**
     * Source dictionary identifier
     */
    @Column(length = 50)
    private String source = "odvp";

    /**
     * Priority score from original Yomichan dictionary
     */
    @Column(nullable = false)
    private Integer priority = 0;

    /**
     * How many times this word has been searched
     */
    @Column(nullable = false)
    private Integer searchCount = 0;

    /**
     * Last time this word was searched
     */
    @Column
    private LocalDateTime lastSearched;

    /**
     * When this entry was created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When this entry was last updated
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.searchCount == null) {
            this.searchCount = 0;
        }
        if (this.priority == null) {
            this.priority = 0;
        }
        if (this.source == null) {
            this.source = "odvp";
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Increment search count and update last searched timestamp
     */
    public void incrementSearchCount() {
        this.searchCount++;
        this.lastSearched = LocalDateTime.now();
    }
}
