package org.example.nihongobackend.entity.blog;

/**
 * Publication status of blog posts
 */
public enum PostStatus {
    /**
     * Draft - not visible to public
     */
    DRAFT("Bản nháp"),

    /**
     * Published - visible to users based on access level
     */
    PUBLISHED("Đã xuất bản"),

    /**
     * Archived - hidden from public but kept in database
     */
    ARCHIVED("Đã lưu trữ");

    private final String displayName;

    PostStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPublic() {
        return this == PUBLISHED;
    }
}
