package org.example.nihongobackend.entity;

/**
 * User roles in the system (4-tier hierarchy)
 *
 * Hierarchy: FREE < PREMIUM < MODERATOR < ADMIN
 * - FREE: Basic user (trial, limited features)
 * - PREMIUM: Paid user (unlimited features)
 * - MODERATOR: Content manager (blog posts, community lessons)
 * - ADMIN: System administrator (users, staff, config, dashboard)
 */
public enum UserRole {
    /**
     * FREE tier - Basic user with limited features
     * - Max 500 flashcards
     * - 50 AI vocabulary lookups/day
     * - 3 Learning Projects max
     * - 10MB storage
     */
    FREE,

    /**
     * PREMIUM tier - Paid user with unlimited features
     * - Unlimited flashcards
     * - Unlimited AI vocabulary lookups
     * - Unlimited Learning Projects
     * - 1GB storage
     * - Export/Import
     * - Advanced analytics
     * - Access to premium community lessons
     */
    PREMIUM,

    /**
     * MODERATOR - Content manager (blog & community lessons)
     *
     * Permissions:
     * - All PREMIUM features
     * - BLOG MANAGEMENT:
     *   - Create/Edit/Delete blog posts
     *   - Manage categories & tags
     *   - Schedule posts
     *   - Moderate comments on blog
     * - COMMUNITY LESSONS:
     *   - Upload video lessons
     *   - Create lesson resources (PDF, docs)
     *   - Organize lesson playlists
     *   - Moderate user comments on lessons
     * - CANNOT: Manage users, change system config, access admin dashboard
     */
    MODERATOR,

    /**
     * ADMIN - System administrator
     *
     * Permissions:
     * - All MODERATOR features
     * - USER & STAFF MANAGEMENT:
     *   - View all users
     *   - Create/Edit/Delete users
     *   - Assign/Revoke roles (FREE, PREMIUM, MODERATOR)
     *   - Manage staff accounts
     *   - Ban/Suspend users
     * - DASHBOARD:
     *   - User growth analytics
     *   - Revenue reports
     *   - System health monitoring
     *   - Engagement metrics
     * - SYSTEM CONFIG:
     *   - API keys (DeepSeek, Claude, etc.)
     *   - Quota limits (free/premium)
     *   - Feature flags
     *   - Email templates
     *   - Payment settings
     * - DATABASE & SECURITY:
     *   - Database backups
     *   - Audit logs
     *   - Security settings
     */
    ADMIN;

    /**
     * Check if role has at least PREMIUM access
     */
    public boolean isPremiumOrAbove() {
        return this == PREMIUM || this == MODERATOR || this == ADMIN;
    }

    /**
     * Check if role has moderator access (content management)
     */
    public boolean isModeratorOrAbove() {
        return this == MODERATOR || this == ADMIN;
    }

    /**
     * Check if role is admin (system management)
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Get human-readable name (Vietnamese)
     */
    public String getDisplayName() {
        return switch (this) {
            case FREE -> "Học viên cơ bản";
            case PREMIUM -> "Học viên Pro";
            case MODERATOR -> "Quản lý nội dung";
            case ADMIN -> "Quản trị viên";
        };
    }

    /**
     * Get role description
     */
    public String getDescription() {
        return switch (this) {
            case FREE -> "Học viên miễn phí với tính năng giới hạn";
            case PREMIUM -> "Học viên Pro với tất cả tính năng cao cấp";
            case MODERATOR -> "Quản lý blog và bài học cộng đồng";
            case ADMIN -> "Quản trị viên hệ thống: users, config, dashboard";
        };
    }
}
