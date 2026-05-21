package org.example.nihongobackend.entity.blog;

/**
 * Access level for blog posts
 */
public enum AccessLevel {
    /**
     * Free - accessible to all users
     */
    FREE("Miễn phí"),

    /**
     * Premium - requires PREMIUM subscription or higher
     */
    PREMIUM("Dành cho Premium");

    private final String displayName;

    AccessLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isFree() {
        return this == FREE;
    }

    public boolean requiresPremium() {
        return this == PREMIUM;
    }
}
