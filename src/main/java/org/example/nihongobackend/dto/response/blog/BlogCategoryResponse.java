package org.example.nihongobackend.dto.response.blog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nihongobackend.entity.blog.BlogCategory;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogCategoryResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long parentId;

    /**
     * Convert entity to response DTO
     */
    public static BlogCategoryResponse fromEntity(BlogCategory category) {
        return BlogCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }
}
