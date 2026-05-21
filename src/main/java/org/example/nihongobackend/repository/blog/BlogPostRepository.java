package org.example.nihongobackend.repository.blog;

import org.example.nihongobackend.entity.blog.BlogPost;
import org.example.nihongobackend.entity.blog.PostStatus;
import org.example.nihongobackend.entity.blog.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    /**
     * Find post by slug
     */
    Optional<BlogPost> findBySlug(String slug);

    /**
     * Find all published posts (public view)
     */
    Page<BlogPost> findByStatusOrderByPublishedAtDesc(PostStatus status, Pageable pageable);

    /**
     * Find published posts by type
     */
    Page<BlogPost> findByStatusAndPostTypeOrderByPublishedAtDesc(
            PostStatus status,
            PostType postType,
            Pageable pageable
    );

    /**
     * Find posts by author
     */
    Page<BlogPost> findByAuthorIdOrderByCreatedAtDesc(
            Long authorId,
            Pageable pageable
    );

    /**
     * Search posts by title or content
     */
    @Query("SELECT p FROM BlogPost p WHERE p.status = :status AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.publishedAt DESC")
    Page<BlogPost> searchPublishedPosts(
            @Param("status") PostStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * Count posts by status
     */
    long countByStatus(PostStatus status);

    /**
     * Count posts by type
     */
    long countByPostType(PostType postType);

    /**
     * Find top viewed posts
     */
    Page<BlogPost> findByStatusOrderByViewsCountDesc(PostStatus status, Pageable pageable);
}
