package org.example.nihongobackend.service.blog;

import org.example.nihongobackend.dto.request.blog.CreateBlogPostRequest;
import org.example.nihongobackend.dto.request.blog.CreateCommentRequest;
import org.example.nihongobackend.dto.request.blog.UpdateBlogPostRequest;
import org.example.nihongobackend.dto.response.blog.BlogCommentResponse;
import org.example.nihongobackend.dto.response.blog.BlogPostListResponse;
import org.example.nihongobackend.dto.response.blog.BlogPostResponse;
import org.example.nihongobackend.entity.blog.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BlogService {

    /**
     * Create a new blog post (ADMIN only)
     */
    BlogPostResponse createPost(CreateBlogPostRequest request, String authorEmail);

    /**
     * Update an existing blog post (ADMIN only)
     */
    BlogPostResponse updatePost(Long postId, UpdateBlogPostRequest request);

    /**
     * Delete a blog post (ADMIN only)
     */
    void deletePost(Long postId);

    /**
     * Get post by slug (public if published, otherwise ADMIN only)
     * Increments view count for published posts
     */
    BlogPostResponse getPostBySlug(String slug, String email);

    /**
     * React to a post (LIKE or DISLIKE)
     */
    BlogPostResponse reactToPost(String slug, org.example.nihongobackend.entity.blog.ReactionType type, String email);

    /**
     * Get post by ID (ADMIN only, doesn't increment view count)
     */
    BlogPostResponse getPostById(Long postId);

    /**
     * Get all published posts (public)
     */
    Page<BlogPostListResponse> getPublishedPosts(Pageable pageable);

    /**
     * Get published posts by type
     */
    Page<BlogPostListResponse> getPublishedPostsByType(PostType postType, Pageable pageable);

    /**
     * Search published posts by keyword
     */
    Page<BlogPostListResponse> searchPosts(String keyword, Pageable pageable);

    /**
     * Get top viewed posts
     */
    Page<BlogPostListResponse> getTopViewedPosts(Pageable pageable);

    /**
     * Get all posts (ADMIN only - includes drafts, archived)
     */
    Page<BlogPostListResponse> getAllPosts(Pageable pageable);

    /**
     * Create a comment on a post
     * Only PREMIUM+ users can comment
     */
    BlogCommentResponse createComment(Long postId, CreateCommentRequest request, String email);

    /**
     * Get approved comments for a post
     */
    List<BlogCommentResponse> getPostComments(Long postId);

    /**
     * Get pending comments (ADMIN only)
     */
    Page<BlogCommentResponse> getPendingComments(Pageable pageable);

    /**
     * Approve a comment (ADMIN only)
     */
    BlogCommentResponse approveComment(Long commentId);

    /**
     * Reject a comment (ADMIN only)
     */
    BlogCommentResponse rejectComment(Long commentId);

    /**
     * Delete a comment (ADMIN only)
     */
    void deleteComment(Long commentId);

    /**
     * Get all categories (public, cached)
     */
    List<org.example.nihongobackend.dto.response.blog.BlogCategoryResponse> getAllCategories();

    /**
     * Get all tags (public, cached)
     */
    List<org.example.nihongobackend.dto.response.blog.BlogTagResponse> getAllTags();
}
