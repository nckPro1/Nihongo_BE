-- Tham chiếu: Hibernate ddl-auto=update thường tự tạo.
-- Bảng learning_projects + cột flashcards.project_id (FK learning_projects, users giữ nguyên).

CREATE TABLE IF NOT EXISTS learning_projects (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_learning_projects_user_created ON learning_projects (user_id, created_at DESC);

-- ALTER TABLE flashcards ADD COLUMN IF NOT EXISTS project_id UUID REFERENCES learning_projects (id);
-- (Cú pháp IF NOT EXISTS cho ADD COLUMN tùy phiên bản PostgreSQL)
