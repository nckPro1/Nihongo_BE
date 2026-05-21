-- Chạy một lần sau khi deploy entity CustomerProfile (bảng customer_profiles đã tồn tại).
-- Copy jlpt / pro từ users sang customer_profiles, rồi (tuỳ chọn) xóa cột cũ trên users.

INSERT INTO customer_profiles (user_id, jlpt_level, is_pro, pro_expires_at, created_at, updated_at)
SELECT u.id,
       COALESCE(NULLIF(TRIM(u.jlpt_level), ''), 'N5'),
       COALESCE(u.is_pro, false),
       u.pro_expires_at,
       u.created_at,
       u.updated_at
FROM users u
WHERE NOT EXISTS (SELECT 1 FROM customer_profiles cp WHERE cp.user_id = u.id);

-- Tuỳ chọn — chỉ chạy khi chắc chắn không còn code đọc các cột này trên users:
-- ALTER TABLE users DROP COLUMN IF EXISTS jlpt_level;
-- ALTER TABLE users DROP COLUMN IF EXISTS is_pro;
-- ALTER TABLE users DROP COLUMN IF EXISTS pro_expires_at;
