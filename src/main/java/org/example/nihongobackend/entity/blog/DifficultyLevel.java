package org.example.nihongobackend.entity.blog;

/**
 * Difficulty level for video lessons
 */
public enum DifficultyLevel {
    /**
     * Beginner level (N5-N4)
     */
    BEGINNER("Sơ cấp"),

    /**
     * Intermediate level (N3-N2)
     */
    INTERMEDIATE("Trung cấp"),

    /**
     * Advanced level (N1+)
     */
    ADVANCED("Nâng cao");

    private final String displayName;

    DifficultyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
