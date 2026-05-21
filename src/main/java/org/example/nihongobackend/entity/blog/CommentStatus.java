package org.example.nihongobackend.entity.blog;

/**
 * Moderation status for blog comments
 */
public enum CommentStatus {
    /**
     * Pending moderation
     */
    PENDING("Chờ duyệt"),

    /**
     * Approved and visible
     */
    APPROVED("Đã duyệt"),

    /**
     * Rejected/hidden
     */
    REJECTED("Đã từ chối");

    private final String displayName;

    CommentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isVisible() {
        return this == APPROVED;
    }
}
