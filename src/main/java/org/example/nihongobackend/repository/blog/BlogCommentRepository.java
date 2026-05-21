package org.example.nihongobackend.repository.blog;

import org.example.nihongobackend.entity.blog.BlogComment;
import org.example.nihongobackend.entity.blog.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {

    /**
     * Find comments by post (only approved)
     */
    List<BlogComment> findByPostIdAndStatusOrderByCreatedAtAsc(Long postId, CommentStatus status);

    /**
     * Find all comments by post (for admin)
     */
    List<BlogComment> findByPostIdOrderByCreatedAtDesc(Long postId);

    /**
     * Find pending comments (for moderation)
     */
    Page<BlogComment> findByStatusOrderByCreatedAtAsc(CommentStatus status, Pageable pageable);

    /**
     * Count pending comments
     */
    long countByStatus(CommentStatus status);

    /**
     * Count comments on a post
     */
    long countByPostIdAndStatus(Long postId, CommentStatus status);

    /**
     * Find comments by user
     */
    List<BlogComment> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
