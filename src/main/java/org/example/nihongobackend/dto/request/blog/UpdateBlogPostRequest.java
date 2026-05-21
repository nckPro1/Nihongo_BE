package org.example.nihongobackend.dto.request.blog;

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
public class UpdateBlogPostRequest {

    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    private String content;

    @Size(max = 1000, message = "Excerpt must not exceed 1000 characters")
    private String excerpt;

    private PostType postType;

    // Video lesson fields
    private String videoUrl;
    private Integer videoDurationMinutes;

    private String featuredImage;

    private PostStatus status;

    // SEO fields
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    // Tags (list of tag names, null = don't update)
    private List<String> tags;
}
