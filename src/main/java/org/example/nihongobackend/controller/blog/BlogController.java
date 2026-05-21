package org.example.nihongobackend.controller.blog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nihongobackend.dto.request.blog.CreateBlogPostRequest;
import org.example.nihongobackend.dto.request.blog.CreateCommentRequest;
import org.example.nihongobackend.dto.request.blog.UpdateBlogPostRequest;
import org.example.nihongobackend.dto.response.blog.BlogCommentResponse;
import org.example.nihongobackend.dto.response.blog.BlogPostListResponse;
import org.example.nihongobackend.dto.response.blog.BlogPostResponse;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.entity.blog.PostType;
import org.example.nihongobackend.service.blog.BlogService;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
@Slf4j
public class BlogController {

    private final BlogService blogService;

    // ========== Public Endpoints ==========

    /**
     * Get all published posts (public)
     */
    @GetMapping("/posts")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<Page<BlogPostListResponse>>> getPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        log.info("GET /api/blog/posts - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPostListResponse> posts = blogService.getPublishedPosts(pageable);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Posts retrieved successfully", posts)
        );
    }

    /**
     * Get published posts by type
     */
    @GetMapping("/posts/type/{type}")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<Page<BlogPostListResponse>>> getPostsByType(
            @PathVariable PostType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        log.info("GET /api/blog/posts/type/{} - page: {}, size: {}", type, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPostListResponse> posts = blogService.getPublishedPostsByType(type, pageable);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Posts retrieved successfully", posts)
        );
    }

    /**
     * Search posts by keyword
     */
    @GetMapping("/posts/search")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<Page<BlogPostListResponse>>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        log.info("GET /api/blog/posts/search?keyword={} - page: {}, size: {}", keyword, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPostListResponse> posts = blogService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Posts retrieved successfully", posts)
        );
    }

    /**
     * Get top viewed posts
     */
    @GetMapping("/posts/top")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<Page<BlogPostListResponse>>> getTopViewedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("GET /api/blog/posts/top - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPostListResponse> posts = blogService.getTopViewedPosts(pageable);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Posts retrieved successfully", posts)
        );
    }

    /**
     * Get post by slug (public)
     * Increments view count
     */
    @GetMapping("/posts/{slug}")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<BlogPostResponse>> getPostBySlug(
            @PathVariable String slug,
            Authentication authentication
    ) {
        log.info("GET /api/blog/posts/{}", slug);
        String email = null;
        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            email = authentication.getName();
        }
        BlogPostResponse post = blogService.getPostBySlug(slug, email);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Post retrieved successfully", post)
        );
    }

    /**
     * Get approved comments for a post
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<List<BlogCommentResponse>>> getPostComments(@PathVariable Long postId) {
        log.info("GET /api/blog/posts/{}/comments", postId);
        List<BlogCommentResponse> comments = blogService.getPostComments(postId);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Comments retrieved successfully", comments)
        );
    }

    // ========== PREMIUM+ Endpoints ==========

    /**
     * Create a comment (requires PREMIUM+)
     */
    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<BlogCommentResponse>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        log.info("POST /api/blog/posts/{}/comments - user: {}", postId, email);
        BlogCommentResponse comment = blogService.createComment(postId, request, email);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Comment created successfully", comment)
        );
    }

    /**
     * React to a post (requires authenticated user)
     */
    @PostMapping("/posts/{slug}/react")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<BlogPostResponse>> reactToPost(
            @PathVariable String slug,
            @RequestParam org.example.nihongobackend.entity.blog.ReactionType type,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        log.info("POST /api/blog/posts/{}/react?type={} - user: {}", slug, type, email);
        BlogPostResponse response = blogService.reactToPost(slug, type, email);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Reaction updated successfully", response)
        );
    }

    // ========== ADMIN Endpoints ==========

    /**
     * Get all posts including drafts (ADMIN only)
     */
    @GetMapping("/admin/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<Page<BlogPostListResponse>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /api/blog/admin/posts - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPostListResponse> posts = blogService.getAllPosts(pageable);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Posts retrieved successfully", posts)
        );
    }

    /**
     * Get post by ID (ADMIN only)
     */
    @GetMapping("/admin/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<BlogPostResponse>> getPostById(@PathVariable Long postId) {
        log.info("GET /api/blog/admin/posts/{}", postId);
        BlogPostResponse post = blogService.getPostById(postId);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Post retrieved successfully", post)
        );
    }

    /**
     * Create a new blog post (ADMIN only)
     */
    @PostMapping("/admin/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<BlogPostResponse>> createPost(
            @Valid @RequestBody CreateBlogPostRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        log.info("POST /api/blog/admin/posts - title: {}, author: {}", request.getTitle(), email);
        BlogPostResponse post = blogService.createPost(request, email);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Post created successfully", post)
        );
    }

    /**
     * Update a blog post (ADMIN only)
     */
    @PutMapping("/admin/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<BlogPostResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdateBlogPostRequest request
    ) {
        log.info("PUT /api/blog/admin/posts/{}", postId);
        BlogPostResponse post = blogService.updatePost(postId, request);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Post updated successfully", post)
        );
    }

    /**
     * Delete a blog post (ADMIN only)
     */
    @DeleteMapping("/admin/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<Void>> deletePost(@PathVariable Long postId) {
        log.info("DELETE /api/blog/admin/posts/{}", postId);
        blogService.deletePost(postId);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Post deleted successfully", null)
        );
    }

    /**
     * Get pending comments (ADMIN only)
     */
    @GetMapping("/admin/comments/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<Page<BlogCommentResponse>>> getPendingComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /api/blog/admin/comments/pending - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogCommentResponse> comments = blogService.getPendingComments(pageable);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Comments retrieved successfully", comments)
        );
    }

    /**
     * Approve a comment (ADMIN only)
     */
    @PutMapping("/admin/comments/{commentId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<BlogCommentResponse>> approveComment(@PathVariable Long commentId) {
        log.info("PUT /api/blog/admin/comments/{}/approve", commentId);
        BlogCommentResponse comment = blogService.approveComment(commentId);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Comment approved successfully", comment)
        );
    }

    /**
     * Reject a comment (ADMIN only)
     */
    @PutMapping("/admin/comments/{commentId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<BlogCommentResponse>> rejectComment(@PathVariable Long commentId) {
        log.info("PUT /api/blog/admin/comments/{}/reject", commentId);
        BlogCommentResponse comment = blogService.rejectComment(commentId);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Comment rejected successfully", comment)
        );
    }

    /**
     * Delete a comment (ADMIN only)
     */
    @DeleteMapping("/admin/comments/{commentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        log.info("DELETE /api/blog/admin/comments/{}", commentId);
        blogService.deleteComment(commentId);
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Comment deleted successfully", null)
        );
    }

    // ========== Categories & Tags (Public, Cached) ==========

    /**
     * Get all categories (public, cached)
     */
    @GetMapping("/categories")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<List<org.example.nihongobackend.dto.response.blog.BlogCategoryResponse>>> getCategories() {
        log.info("GET /api/blog/categories");
        List<org.example.nihongobackend.dto.response.blog.BlogCategoryResponse> categories = blogService.getAllCategories();
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Categories retrieved successfully", categories)
        );
    }

    /**
     * Get all tags (public, cached)
     */
    @GetMapping("/tags")
    public ResponseEntity<org.example.nihongobackend.dto.response.common.ApiResponse<List<org.example.nihongobackend.dto.response.blog.BlogTagResponse>>> getTags() {
        log.info("GET /api/blog/tags");
        List<org.example.nihongobackend.dto.response.blog.BlogTagResponse> tags = blogService.getAllTags();
        return ResponseEntity.ok(
            org.example.nihongobackend.dto.response.common.ApiResponse.success("Tags retrieved successfully", tags)
        );
    }
}
