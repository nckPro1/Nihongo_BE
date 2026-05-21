package org.example.nihongobackend.service.blog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nihongobackend.dto.request.blog.CreateBlogPostRequest;
import org.example.nihongobackend.dto.request.blog.CreateCommentRequest;
import org.example.nihongobackend.dto.request.blog.UpdateBlogPostRequest;
import org.example.nihongobackend.dto.response.blog.BlogCommentResponse;
import org.example.nihongobackend.dto.response.blog.BlogPostListResponse;
import org.example.nihongobackend.dto.response.blog.BlogPostResponse;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.entity.UserRole;
import org.example.nihongobackend.entity.blog.*;
import org.example.nihongobackend.exception.BadRequestException;
import org.example.nihongobackend.exception.ForbiddenException;
import org.example.nihongobackend.exception.NotFoundException;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.repository.blog.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogServiceImpl implements BlogService {

    private final BlogPostRepository blogPostRepository;
    private final BlogCategoryRepository blogCategoryRepository;
    private final BlogTagRepository blogTagRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final UserRepository userRepository;
    private final BlogPostReactionRepository blogPostReactionRepository;

    @Override
    @Transactional
    public BlogPostResponse createPost(CreateBlogPostRequest request, String authorEmail) {
        log.info("Creating blog post: {} by author: {}", request.getTitle(), authorEmail);

        // Validate request
        if (!request.isValid()) {
            throw new BadRequestException("Video URL is required for VIDEO_LESSON post type");
        }

        // Get author by email
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check author is ADMIN
        if (!author.getRole().isAdmin()) {
            throw new ForbiddenException("Only ADMIN can create blog posts");
        }

        // Build post
        BlogPost post = BlogPost.builder()
                .title(request.getTitle())
                .slug(generateSlug(request.getTitle()))
                .content(request.getContent())
                .excerpt(request.getExcerpt())
                .postType(request.getPostType())
                .videoUrl(request.getVideoUrl())
                .videoDurationMinutes(request.getVideoDurationMinutes())
                .featuredImage(request.getFeaturedImage())
                .author(author)
                .status(request.getStatus())
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .build();

        // Set published time if status is PUBLISHED
        if (post.getStatus() == PostStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }

        // Save post first
        post = blogPostRepository.save(post);

        // Handle tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<BlogTag> tags = getOrCreateTags(request.getTags());
            for (BlogTag tag : tags) {
                post.addTag(tag);
            }
            post = blogPostRepository.save(post);
        }

        log.info("Blog post created with ID: {}", post.getId());
        return BlogPostResponse.fromEntity(post);
    }

    @Override
    @Transactional
    public BlogPostResponse updatePost(Long postId, UpdateBlogPostRequest request) {
        log.info("Updating blog post: {}", postId);

        BlogPost post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Blog post not found"));

        // Update fields if provided
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
            post.setSlug(generateSlug(request.getTitle()));
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getExcerpt() != null) {
            post.setExcerpt(request.getExcerpt());
        }
        if (request.getPostType() != null) {
            post.setPostType(request.getPostType());
        }
        if (request.getVideoUrl() != null) {
            post.setVideoUrl(request.getVideoUrl());
        }
        if (request.getVideoDurationMinutes() != null) {
            post.setVideoDurationMinutes(request.getVideoDurationMinutes());
        }
        if (request.getFeaturedImage() != null) {
            post.setFeaturedImage(request.getFeaturedImage());
        }
        if (request.getMetaTitle() != null) {
            post.setMetaTitle(request.getMetaTitle());
        }
        if (request.getMetaDescription() != null) {
            post.setMetaDescription(request.getMetaDescription());
        }
        if (request.getMetaKeywords() != null) {
            post.setMetaKeywords(request.getMetaKeywords());
        }

        // Update status and published time
        if (request.getStatus() != null) {
            PostStatus oldStatus = post.getStatus();
            post.setStatus(request.getStatus());

            // Set published time when changing from DRAFT to PUBLISHED
            if (oldStatus == PostStatus.DRAFT && request.getStatus() == PostStatus.PUBLISHED) {
                post.setPublishedAt(LocalDateTime.now());
            }
        }

        // Update tags if provided
        if (request.getTags() != null) {
            // Clear existing tags
            new ArrayList<>(post.getTags()).forEach(post::removeTag);

            // Add new tags
            if (!request.getTags().isEmpty()) {
                Set<BlogTag> newTags = getOrCreateTags(request.getTags());
                for (BlogTag tag : newTags) {
                    post.addTag(tag);
                }
            }
        }

        post = blogPostRepository.save(post);
        log.info("Blog post updated: {}", postId);

        return BlogPostResponse.fromEntity(post);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        log.info("Deleting blog post: {}", postId);

        if (!blogPostRepository.existsById(postId)) {
            throw new NotFoundException("Blog post not found");
        }

        blogPostRepository.deleteById(postId);
        log.info("Blog post deleted: {}", postId);
    }

    @Override
    @Transactional
    public BlogPostResponse getPostBySlug(String slug, String email) {
        BlogPost post = blogPostRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Blog post not found"));

        User user = null;
        if (email != null && !email.isBlank()) {
            user = userRepository.findByEmail(email).orElse(null);
        }

        // Check access permissions
        if (post.getStatus() != PostStatus.PUBLISHED) {
            // Only ADMIN can view unpublished posts
            if (user == null) {
                throw new ForbiddenException("This post is not published");
            }
            if (!user.getRole().isAdmin()) {
                throw new ForbiddenException("This post is not published");
            }
        }

        // Increment view count for published posts
        if (post.isPublished()) {
            post.incrementViews();
            blogPostRepository.save(post);
        }

        ReactionType reaction = null;
        if (user != null) {
            reaction = blogPostReactionRepository.findByPostIdAndUserId(post.getId(), user.getId())
                    .map(BlogPostReaction::getType)
                    .orElse(null);
        }

        return BlogPostResponse.fromEntity(post, reaction);
    }

    @Override
    @Transactional
    public BlogPostResponse reactToPost(String slug, ReactionType type, String email) {
        log.info("User {} reacting with {} to post: {}", email, type, slug);
        
        BlogPost post = blogPostRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Blog post not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Optional<BlogPostReaction> existingReactionOpt = blogPostReactionRepository.findByPostIdAndUserId(post.getId(), user.getId());
        ReactionType resultingReaction = type;

        if (existingReactionOpt.isEmpty()) {
            // Case 1: No prior reaction
            BlogPostReaction reaction = BlogPostReaction.builder()
                    .post(post)
                    .user(user)
                    .type(type)
                    .build();
            blogPostReactionRepository.save(reaction);
            if (type == ReactionType.LIKE) {
                post.setLikesCount((post.getLikesCount() != null ? post.getLikesCount() : 0) + 1);
            } else {
                post.setDislikesCount((post.getDislikesCount() != null ? post.getDislikesCount() : 0) + 1);
            }
        } else {
            BlogPostReaction existingReaction = existingReactionOpt.get();
            if (existingReaction.getType() == type) {
                // Case 2: Clicked the same reaction, so toggle it OFF (remove reaction)
                blogPostReactionRepository.delete(existingReaction);
                if (type == ReactionType.LIKE) {
                    post.setLikesCount(Math.max(0, (post.getLikesCount() != null ? post.getLikesCount() : 0) - 1));
                } else {
                    post.setDislikesCount(Math.max(0, (post.getDislikesCount() != null ? post.getDislikesCount() : 0) - 1));
                }
                resultingReaction = null;
            } else {
                // Case 3: Clicked the opposite reaction, so flip it
                existingReaction.setType(type);
                blogPostReactionRepository.save(existingReaction);
                if (type == ReactionType.LIKE) {
                    post.setLikesCount((post.getLikesCount() != null ? post.getLikesCount() : 0) + 1);
                    post.setDislikesCount(Math.max(0, (post.getDislikesCount() != null ? post.getDislikesCount() : 0) - 1));
                } else {
                    post.setDislikesCount((post.getDislikesCount() != null ? post.getDislikesCount() : 0) + 1);
                    post.setLikesCount(Math.max(0, (post.getLikesCount() != null ? post.getLikesCount() : 0) - 1));
                }
            }
        }

        post = blogPostRepository.save(post);
        return BlogPostResponse.fromEntity(post, resultingReaction);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogPostResponse getPostById(Long postId) {
        BlogPost post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Blog post not found"));
        return BlogPostResponse.fromEntity(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogPostListResponse> getPublishedPosts(Pageable pageable) {
        Page<BlogPost> posts = blogPostRepository.findByStatusOrderByPublishedAtDesc(
                PostStatus.PUBLISHED,
                pageable
        );
        return posts.map(BlogPostListResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogPostListResponse> getPublishedPostsByType(PostType postType, Pageable pageable) {
        Page<BlogPost> posts = blogPostRepository.findByStatusAndPostTypeOrderByPublishedAtDesc(
                PostStatus.PUBLISHED,
                postType,
                pageable
        );
        return posts.map(BlogPostListResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogPostListResponse> searchPosts(String keyword, Pageable pageable) {
        Page<BlogPost> posts = blogPostRepository.searchPublishedPosts(
                PostStatus.PUBLISHED,
                keyword,
                pageable
        );
        return posts.map(BlogPostListResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogPostListResponse> getTopViewedPosts(Pageable pageable) {
        Page<BlogPost> posts = blogPostRepository.findByStatusOrderByViewsCountDesc(
                PostStatus.PUBLISHED,
                pageable
        );
        return posts.map(BlogPostListResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogPostListResponse> getAllPosts(Pageable pageable) {
        Page<BlogPost> posts = blogPostRepository.findAll(pageable);
        return posts.map(BlogPostListResponse::fromEntity);
    }

    @Override
    @Transactional
    public BlogCommentResponse createComment(Long postId, CreateCommentRequest request, String email) {
        log.info("Creating comment on post: {} by user: {}", postId, email);

        // Get post
        BlogPost post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Blog post not found"));

        // Get user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check user is PREMIUM+ (only PREMIUM and above can comment)
        if (!user.getRole().isPremiumOrAbove()) {
            throw new ForbiddenException("Only PREMIUM users can comment");
        }

        // Create comment
        BlogComment comment = BlogComment.builder()
                .post(post)
                .user(user)
                .content(request.getContent())
                .status(CommentStatus.PENDING)
                .build();

        comment = blogCommentRepository.save(comment);
        log.info("Comment created with ID: {}", comment.getId());

        return BlogCommentResponse.fromEntity(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogCommentResponse> getPostComments(Long postId) {
        List<BlogComment> comments = blogCommentRepository.findByPostIdAndStatusOrderByCreatedAtAsc(
                postId,
                CommentStatus.APPROVED
        );
        return comments.stream()
                .map(BlogCommentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogCommentResponse> getPendingComments(Pageable pageable) {
        Page<BlogComment> comments = blogCommentRepository.findByStatusOrderByCreatedAtAsc(
                CommentStatus.PENDING,
                pageable
        );
        return comments.map(BlogCommentResponse::fromEntity);
    }

    @Override
    @Transactional
    public BlogCommentResponse approveComment(Long commentId) {
        log.info("Approving comment: {}", commentId);

        BlogComment comment = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        comment.approve();
        comment = blogCommentRepository.save(comment);

        log.info("Comment approved: {}", commentId);
        return BlogCommentResponse.fromEntity(comment);
    }

    @Override
    @Transactional
    public BlogCommentResponse rejectComment(Long commentId) {
        log.info("Rejecting comment: {}", commentId);

        BlogComment comment = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        comment.reject();
        comment = blogCommentRepository.save(comment);

        log.info("Comment rejected: {}", commentId);
        return BlogCommentResponse.fromEntity(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        log.info("Deleting comment: {}", commentId);

        if (!blogCommentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment not found");
        }

        blogCommentRepository.deleteById(commentId);
        log.info("Comment deleted: {}", commentId);
    }

    // Helper methods

    /**
     * Generate URL-friendly slug from title
     */
    private String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a")
                .replaceAll("[éèẻẽẹêếềểễệ]", "e")
                .replaceAll("[íìỉĩị]", "i")
                .replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o")
                .replaceAll("[úùủũụưứừửữự]", "u")
                .replaceAll("[ýỳỷỹỵ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        // Ensure uniqueness
        String baseSlug = slug;
        int counter = 1;
        while (blogPostRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }

    /**
     * Get existing tags or create new ones
     */
    private Set<BlogTag> getOrCreateTags(List<String> tagNames) {
        Set<BlogTag> tags = new HashSet<>();

        for (String tagName : tagNames) {
            BlogTag tag = blogTagRepository.findByNameIgnoreCase(tagName)
                    .orElseGet(() -> {
                        BlogTag newTag = BlogTag.builder()
                                .name(tagName)
                                .slug(generateTagSlug(tagName))
                                .build();
                        return blogTagRepository.save(newTag);
                    });
            tags.add(tag);
        }

        return tags;
    }

    /**
     * Generate slug for tag
     */
    private String generateTagSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a")
                .replaceAll("[éèẻẽẹêếềểễệ]", "e")
                .replaceAll("[íìỉĩị]", "i")
                .replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o")
                .replaceAll("[úùủũụưứừửữự]", "u")
                .replaceAll("[ýỳỷỹỵ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    @Override
    public List<org.example.nihongobackend.dto.response.blog.BlogCategoryResponse> getAllCategories() {
        log.info("Getting all categories");
        return blogCategoryRepository.findAll().stream()
                .map(org.example.nihongobackend.dto.response.blog.BlogCategoryResponse::fromEntity)
                .toList();
    }

    @Override
    public List<org.example.nihongobackend.dto.response.blog.BlogTagResponse> getAllTags() {
        log.info("Getting all tags");
        return blogTagRepository.findAll().stream()
                .map(org.example.nihongobackend.dto.response.blog.BlogTagResponse::fromEntity)
                .toList();
    }
}
