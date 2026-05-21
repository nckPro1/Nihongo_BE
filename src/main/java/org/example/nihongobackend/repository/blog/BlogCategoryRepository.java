package org.example.nihongobackend.repository.blog;

import org.example.nihongobackend.entity.blog.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long> {

    /**
     * Find category by slug
     */
    Optional<BlogCategory> findBySlug(String slug);

    /**
     * Find top-level categories (no parent)
     */
    List<BlogCategory> findByParentIsNullOrderByDisplayOrderAsc();

    /**
     * Find child categories of a parent
     */
    List<BlogCategory> findByParentIdOrderByDisplayOrderAsc(Long parentId);

    /**
     * Check if slug exists
     */
    boolean existsBySlug(String slug);
}
