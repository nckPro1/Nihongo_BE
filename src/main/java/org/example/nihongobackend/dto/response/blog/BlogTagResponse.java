package org.example.nihongobackend.dto.response.blog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nihongobackend.entity.blog.BlogTag;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogTagResponse {

    private Long id;
    private String name;
    private String slug;

    /**
     * Convert entity to response DTO
     */
    public static BlogTagResponse fromEntity(BlogTag tag) {
        return BlogTagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .build();
    }
}
