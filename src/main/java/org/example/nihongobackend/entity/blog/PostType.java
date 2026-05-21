package org.example.nihongobackend.entity.blog;

/**
 * Types of blog posts in the unified content system
 */
public enum PostType {
    /**
     * Standard blog article
     */
    ARTICLE("Bài báo"),

    /**
     * Community video lesson (can be FREE or PREMIUM)
     */
    VIDEO_LESSON("Video học"),

    /**
     * Course advertisement
     */
    COURSE_AD("Quảng cáo khoá học"),

    /**
     * News and updates
     */
    NEWS("Tin tức"),

    /**
     * Tutorial or guide
     */
    TUTORIAL("Hướng dẫn");

    private final String displayName;

    PostType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if this post type supports video content
     */
    public boolean supportsVideo() {
        return this == VIDEO_LESSON || this == TUTORIAL;
    }

    /**
     * Check if this post type requires video URL
     */
    public boolean requiresVideo() {
        return this == VIDEO_LESSON;
    }
}
