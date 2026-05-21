package org.example.nihongobackend.dto.response.blog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nihongobackend.entity.blog.BlogPost;
import org.example.nihongobackend.entity.blog.PostType;
import org.example.nihongobackend.entity.blog.PostStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Simplified blog post response for list views
 * Excludes full content to reduce payload size
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPostListResponse {

    private Long id;
    private String title;
    private String slug;
    private String excerpt;

    private PostType postType;
    private String postTypeDisplay;
    private PostStatus status;

    private String featuredImage;
    private String authorName;

    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private Integer viewsCount;

    private List<String> tags;
    private Integer commentsCount;
    private Integer likesCount;
    private Integer dislikesCount;

    /**
     * Convert entity to list response DTO
     */
    public static BlogPostListResponse fromEntity(BlogPost post) {
        return BlogPostListResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .excerpt(post.getExcerpt())
                .postType(post.getPostType())
                .postTypeDisplay(post.getPostType().getDisplayName())
                .status(post.getStatus())
                .featuredImage(post.getFeaturedImage())
                .authorName(post.getAuthor().getUsername())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .viewsCount(post.getViewsCount())
                .tags(post.getTags().stream().map(tag -> tag.getName()).limit(5).toList())
                .commentsCount(post.getComments() != null ? post.getComments().size() : 0)
                .likesCount(post.getLikesCount() != null ? post.getLikesCount() : 0)
                .dislikesCount(post.getDislikesCount() != null ? post.getDislikesCount() : 0)
                .build();
    }
}
