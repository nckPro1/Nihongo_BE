-- Mẫu: 1 nhóm + 1 mẫu. ID do PostgreSQL sinh (gen_random_uuid); không cần gõ tay UUID.
-- Cần extension (thường đã bật sẵn trên PG mới): CREATE EXTENSION IF NOT EXISTS pgcrypto;

BEGIN;

WITH new_group AS (
  INSERT INTO grammar_groups (id, jlpt_level, name, description, sort_order, created_at)
  VALUES (
    gen_random_uuid(),
    'N4',
    'Nhóm kinh nghiệm / trải nghiệm',
    'Ví dụ: たことがある. Cùng nhóm thì dùng chung group_id, tăng sort_order (0,1,2…).',
    0,
    now()
  )
  RETURNING id
)
INSERT INTO grammar_points (id, group_id, title, formula, meaning, context, note, examples_json, sort_order, created_at)
SELECT
  gen_random_uuid(),
  new_group.id,
  '～たことがある',
  'V-た + ことがある',
  'Đã từng làm gì đó (kinh nghiệm trong quá khứ).',
  'Dùng cho trải nghiệm đã có; không dùng cho việc “vừa mới xong ngay bây giờ”.',
  'Phủ định: たことがない。',
  $json$
[{"ja":"日本に行ったことがあります。","vi":"Tôi đã từng đi Nhật.","register":"polite"},{"ja":"そんなの、見たことない。","vi":"Cái kiểu đó tớ chưa từng thấy.","register":"casual"}]
$json$::text,
  0,
  now()
FROM new_group;

COMMIT;

-- Ghi chú: Hibernate không đặt DEFAULT uuid trên DB, nên INSERT tay phải dùng gen_random_uuid()
-- (hoặc sau này ALTER TABLE ... ALTER COLUMN id SET DEFAULT gen_random_uuid()).
