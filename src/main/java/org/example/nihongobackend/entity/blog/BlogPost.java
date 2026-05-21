package org.example.nihongobackend.entity.blog;

import jakarta.persistence.*;
import lombok.*;
import org.example.nihongobackend.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unified blog post entity supporting multiple content types:
 * - Standard articles
 * - Video lessons
 * - Course advertisements
 * - News and updates
 * - Tutorials
 */
@Entity
@Table(name = "blog_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, unique = true, length = 500)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 1000)
    private String excerpt;

    // Content Type Classification
    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 50)
    private PostType postType;

    // Video Lesson Fields (nullable, used when postType = VIDEO_LESSON)
    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    @Column(name = "video_duration_minutes")
    private Integer videoDurationMinutes;

    // Metadata
    @Column(name = "featured_image", length = 1000)
    private String featuredImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PostStatus status = PostStatus.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "views_count")
    @Builder.Default
    private Integer viewsCount = 0;

    @Column(name = "likes_count")
    @Builder.Default
    private Integer likesCount = 0;

    @Column(name = "dislikes_count")
    @Builder.Default
    private Integer dislikesCount = 0;

    // SEO
    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    // Relationships
    @ManyToMany
    @JoinTable(
            name = "blog_post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<BlogTag> tags = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BlogComment> comments = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isPublished() {
        return this.status == PostStatus.PUBLISHED;
    }

    public boolean isVideoLesson() {
        return this.postType == PostType.VIDEO_LESSON;
    }

    public void incrementViews() {
        this.viewsCount++;
    }

    public void addTag(BlogTag tag) {
        this.tags.add(tag);
        tag.getPosts().add(this);
    }

    public void removeTag(BlogTag tag) {
        this.tags.remove(tag);
        tag.getPosts().remove(this);
    }
}
