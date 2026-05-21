-- Migration: Create Blog System (Unified Content Management)
-- Date: 2026-05-20
-- Description: Blog system supporting multiple content types (articles, video lessons, course ads)

-- Blog Categories
CREATE TABLE blog_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(200) UNIQUE NOT NULL,
    description TEXT,
    parent_id BIGINT REFERENCES blog_categories(id),
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Blog Tags
CREATE TABLE blog_tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Blog Posts (Unified: Articles, Video Lessons, Course Ads, etc.)
CREATE TABLE blog_posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) UNIQUE NOT NULL,
    content TEXT,
    excerpt VARCHAR(1000),

    -- Content Type Classification
    post_type VARCHAR(50) NOT NULL, -- 'ARTICLE', 'VIDEO_LESSON', 'COURSE_AD', 'NEWS', 'TUTORIAL'
    category_id BIGINT REFERENCES blog_categories(id) ON DELETE SET NULL,

    -- Video Lesson Fields
    video_url VARCHAR(1000),
    video_duration_minutes INT,
    difficulty_level VARCHAR(20), -- 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'

    -- Access Control
    access_level VARCHAR(20) NOT NULL DEFAULT 'FREE', -- 'FREE' or 'PREMIUM'

    -- Metadata
    featured_image VARCHAR(1000),
    author_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- 'DRAFT', 'PUBLISHED', 'ARCHIVED'
    published_at TIMESTAMP,
    views_count INT DEFAULT 0,

    -- SEO
    meta_title VARCHAR(200),
    meta_description VARCHAR(500),
    meta_keywords VARCHAR(500),

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT valid_post_type CHECK (post_type IN ('ARTICLE', 'VIDEO_LESSON', 'COURSE_AD', 'NEWS', 'TUTORIAL')),
    CONSTRAINT valid_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT valid_access_level CHECK (access_level IN ('FREE', 'PREMIUM')),
    CONSTRAINT valid_difficulty CHECK (difficulty_level IS NULL OR difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

-- Blog Post Tags (Many-to-Many)
CREATE TABLE blog_post_tags (
    post_id BIGINT NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES blog_tags(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

-- Blog Comments
CREATE TABLE blog_comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'APPROVED', 'REJECTED'
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT valid_comment_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- Indexes for Performance
CREATE INDEX idx_blog_posts_type ON blog_posts(post_type);
CREATE INDEX idx_blog_posts_status ON blog_posts(status);
CREATE INDEX idx_blog_posts_access ON blog_posts(access_level);
CREATE INDEX idx_blog_posts_published ON blog_posts(published_at DESC);
CREATE INDEX idx_blog_posts_author ON blog_posts(author_id);
CREATE INDEX idx_blog_posts_category ON blog_posts(category_id);
CREATE INDEX idx_blog_posts_slug ON blog_posts(slug);

CREATE INDEX idx_blog_comments_post ON blog_comments(post_id);
CREATE INDEX idx_blog_comments_user ON blog_comments(user_id);
CREATE INDEX idx_blog_comments_status ON blog_comments(status);

CREATE INDEX idx_blog_categories_slug ON blog_categories(slug);
CREATE INDEX idx_blog_categories_parent ON blog_categories(parent_id);

-- Seed Default Categories
INSERT INTO blog_categories (name, slug, description, display_order) VALUES
('Học tiếng Nhật', 'hoc-tieng-nhat', 'Bài viết về học tiếng Nhật', 1),
('Ngữ pháp', 'ngu-phap', 'Bài viết về ngữ pháp tiếng Nhật', 2),
('Từ vựng', 'tu-vung', 'Bài viết về từ vựng', 3),
('Video bài học', 'video-bai-hoc', 'Video hướng dẫn học tiếng Nhật', 4),
('Tin tức', 'tin-tuc', 'Tin tức và cập nhật', 5),
('Khoá học', 'khoa-hoc', 'Thông tin về các khoá học', 6);

-- Seed Default Tags
INSERT INTO blog_tags (name, slug) VALUES
('JLPT N5', 'jlpt-n5'),
('JLPT N4', 'jlpt-n4'),
('JLPT N3', 'jlpt-n3'),
('JLPT N2', 'jlpt-n2'),
('JLPT N1', 'jlpt-n1'),
('Kanji', 'kanji'),
('Hiragana', 'hiragana'),
('Katakana', 'katakana'),
('Ngữ pháp', 'ngu-phap'),
('Từ vựng', 'tu-vung'),
('Luyện nghe', 'luyen-nghe'),
('Luyện nói', 'luyen-noi');
