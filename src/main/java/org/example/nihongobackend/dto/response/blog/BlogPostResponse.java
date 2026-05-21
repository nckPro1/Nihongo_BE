package org.example.nihongobackend.dto.response.blog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nihongobackend.entity.blog.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPostResponse {

    private Long id;
    private String title;
    private String slug;
    private String content;
    private String excerpt;

    // Content classification
    private PostType postType;
    private String postTypeDisplay;

    // Video lesson fields
    private String videoUrl;
    private Integer videoDurationMinutes;

    // Metadata
    private String featuredImage;
    private AuthorResponse author;
    private PostStatus status;
    private String statusDisplay;
    private LocalDateTime publishedAt;
    private Integer viewsCount;

    // SEO
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    // Relationships
    private List<BlogTagResponse> tags;
    private Integer commentsCount;

    private Integer likesCount;
    private Integer dislikesCount;
    private String userReaction;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorResponse {
        private UUID id;
        private String username;
        private String email;
    }

    /**
     * Convert entity to response DTO
     */
    public static BlogPostResponse fromEntity(BlogPost post) {
        return fromEntity(post, null);
    }

    /**
     * Convert entity to response DTO with user reaction
     */
    public static BlogPostResponse fromEntity(BlogPost post, ReactionType userReaction) {
        return BlogPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .content(post.getContent())
                .excerpt(post.getExcerpt())
                .postType(post.getPostType())
                .postTypeDisplay(post.getPostType().getDisplayName())
                .videoUrl(post.getVideoUrl())
                .videoDurationMinutes(post.getVideoDurationMinutes())
                .featuredImage(post.getFeaturedImage())
                .author(AuthorResponse.builder()
                        .id(post.getAuthor().getId())
                        .username(post.getAuthor().getUsername())
                        .email(post.getAuthor().getEmail())
                        .build())
                .status(post.getStatus())
                .statusDisplay(post.getStatus().getDisplayName())
                .publishedAt(post.getPublishedAt())
                .viewsCount(post.getViewsCount())
                .metaTitle(post.getMetaTitle())
                .metaDescription(post.getMetaDescription())
                .metaKeywords(post.getMetaKeywords())
                .tags(post.getTags().stream().map(BlogTagResponse::fromEntity).toList())
                .commentsCount(post.getComments() != null ? post.getComments().size() : 0)
                .likesCount(post.getLikesCount() != null ? post.getLikesCount() : 0)
                .dislikesCount(post.getDislikesCount() != null ? post.getDislikesCount() : 0)
                .userReaction(userReaction != null ? userReaction.name() : null)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
