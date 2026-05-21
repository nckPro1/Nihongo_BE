package org.example.nihongobackend.dto.response.blog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nihongobackend.entity.blog.BlogComment;
import org.example.nihongobackend.entity.blog.CommentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogCommentResponse {

    private Long id;
    private Long postId;
    private String postTitle;

    private UserInfo user;
    private String content;

    private CommentStatus status;
    private String statusDisplay;

    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private UUID id;
        private String username;
    }

    /**
     * Convert entity to response DTO
     */
    public static BlogCommentResponse fromEntity(BlogComment comment) {
        return BlogCommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .postTitle(comment.getPost().getTitle())
                .user(UserInfo.builder()
                        .id(comment.getUser().getId())
                        .username(comment.getUser().getUsername())
                        .build())
                .content(comment.getContent())
                .status(comment.getStatus())
                .statusDisplay(comment.getStatus().getDisplayName())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
