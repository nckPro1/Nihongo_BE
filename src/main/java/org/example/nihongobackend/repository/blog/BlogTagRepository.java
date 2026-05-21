package org.example.nihongobackend.repository.blog;

import org.example.nihongobackend.entity.blog.BlogTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogTagRepository extends JpaRepository<BlogTag, Long> {

    /**
     * Find tag by slug
     */
    Optional<BlogTag> findBySlug(String slug);

    /**
     * Find tag by name (case-insensitive)
     */
    Optional<BlogTag> findByNameIgnoreCase(String name);

    /**
     * Find tags by names (for bulk lookup)
     */
    List<BlogTag> findByNameIn(List<String> names);

    /**
     * Check if tag exists by name
     */
    boolean existsByNameIgnoreCase(String name);
}
