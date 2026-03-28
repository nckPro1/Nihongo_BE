-- Bảng flashcard người dùng (Hibernate ddl-auto=update thường tự tạo; file này để tham chiếu / chạy tay nếu cần).
CREATE TABLE IF NOT EXISTS flashcards (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    kanji VARCHAR(2000) NOT NULL,
    reading VARCHAR(1000),
    meaning VARCHAR(4000) NOT NULL,
    direction VARCHAR(16),
    source_query VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_flashcards_user_created ON flashcards (user_id, created_at DESC);
