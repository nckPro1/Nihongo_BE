package org.example.nihongobackend.entity.blog;

import jakarta.persistence.*;
import lombok.*;
import org.example.nihongobackend.entity.User;

import java.time.LocalDateTime;

/**
 * Comment on a blog post
 * Requires moderation by ADMIN
 */
@Entity
@Table(name = "blog_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BlogPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CommentStatus status = CommentStatus.PENDING;

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
    public boolean isApproved() {
        return this.status == CommentStatus.APPROVED;
    }

    public boolean isPending() {
        return this.status == CommentStatus.PENDING;
    }

    public void approve() {
        this.status = CommentStatus.APPROVED;
    }

    public void reject() {
        this.status = CommentStatus.REJECTED;
    }
}
