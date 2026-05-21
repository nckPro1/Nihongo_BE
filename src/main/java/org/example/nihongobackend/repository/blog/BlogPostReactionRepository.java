package org.example.nihongobackend.repository.blog;

import org.example.nihongobackend.entity.blog.BlogPostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogPostReactionRepository extends JpaRepository<BlogPostReaction, Long> {
    Optional<BlogPostReaction> findByPostIdAndUserId(Long postId, UUID userId);
    void deleteByPostIdAndUserId(Long postId, UUID userId);
}
