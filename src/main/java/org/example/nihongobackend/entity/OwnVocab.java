package org.example.nihongobackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Layer 1: Own curated JLPT vocabulary with Vietnamese translations
 * - 7,895 words from Kaggle JLPT dataset
 * - Auto-translated English → Vietnamese
 * - Covers ~60% of typical user queries
 * - <10ms lookup, $0 cost
 */
@Entity
@Table(
    name = "own_vocab",
    indexes = {
        @Index(name = "idx_own_vocab_word", columnList = "word"),
        @Index(name = "idx_own_vocab_reading", columnList = "reading"),
        @Index(name = "idx_own_vocab_level", columnList = "level"),
        @Index(name = "idx_own_vocab_verified", columnList = "is_verified"),
        @Index(name = "idx_own_vocab_search_count", columnList = "search_count")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_word", columnNames = {"word"})
    }
)
public class OwnVocab {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String word;

    @Column(length = 500)
    private String reading;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaningEn;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaningVi;

    @Column(nullable = false, length = 10)
    private String level; // N5, N4, N3, N2, N1

    @Column(length = 50)
    private String partOfSpeech;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column
    private UUID verifiedBy;

    @Column(nullable = false)
    private Integer searchCount = 0;

    @Column
    private LocalDateTime lastSearched;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isVerified == null) this.isVerified = false;
        if (this.searchCount == null) this.searchCount = 0;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getReading() {
        return reading;
    }

    public void setReading(String reading) {
        this.reading = reading;
    }

    public String getMeaningEn() {
        return meaningEn;
    }

    public void setMeaningEn(String meaningEn) {
        this.meaningEn = meaningEn;
    }

    public String getMeaningVi() {
        return meaningVi;
    }

    public void setMeaningVi(String meaningVi) {
        this.meaningVi = meaningVi;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public UUID getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(UUID verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public Integer getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(Integer searchCount) {
        this.searchCount = searchCount;
    }

    public LocalDateTime getLastSearched() {
        return lastSearched;
    }

    public void setLastSearched(LocalDateTime lastSearched) {
        this.lastSearched = lastSearched;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void incrementSearchCount() {
        this.searchCount++;
        this.lastSearched = LocalDateTime.now();
    }
}
