-- Chạy thủ công nếu không dùng Hibernate ddl-auto update.
-- PostgreSQL
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar VARCHAR(2048);

COMMENT ON COLUMN users.avatar IS 'URL ảnh đại diện (Google hoặc file upload)';
