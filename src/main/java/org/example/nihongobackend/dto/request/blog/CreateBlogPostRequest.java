package org.example.nihongobackend.dto.request.blog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nihongobackend.entity.blog.PostStatus;
import org.example.nihongobackend.entity.blog.PostType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBlogPostRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 1000, message = "Excerpt must not exceed 1000 characters")
    private String excerpt;

    @NotNull(message = "Post type is required")
    private PostType postType;

    // Video lesson fields (optional, required if postType = VIDEO_LESSON)
    private String videoUrl;
    private Integer videoDurationMinutes;

    private String featuredImage;

    @NotNull(message = "Status is required")
    @Builder.Default
    private PostStatus status = PostStatus.DRAFT;

    // SEO fields
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    // Tags (list of tag names)
    private List<String> tags;

    /**
     * Validation: Video URL is required for VIDEO_LESSON type
     */
    public boolean isValid() {
        if (postType == PostType.VIDEO_LESSON && (videoUrl == null || videoUrl.isBlank())) {
            return false;
        }
        return true;
    }
}
