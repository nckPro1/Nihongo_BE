-- Migration: Update user roles to 4-tier system (FREE, PREMIUM, MODERATOR, ADMIN)
-- Date: 2026-05-20
-- Description: Replace old 3-role system (USER, PRO, ADMIN) with new 4-tier hierarchy

-- Step 1: Update existing users to new role names
UPDATE users
SET role = CASE
    WHEN role = 'USER' THEN 'FREE'      -- Regular users become FREE tier
    WHEN role = 'PRO' THEN 'PREMIUM'    -- Pro users become PREMIUM tier
    WHEN role = 'ADMIN' THEN 'ADMIN'    -- Admin stays ADMIN
    ELSE 'FREE'                          -- Default to FREE for any unknown roles
END;

-- Step 2: Verify migration (should return 0 rows with old roles)
-- SELECT * FROM users WHERE role NOT IN ('FREE', 'PREMIUM', 'MODERATOR', 'ADMIN');

-- Step 3: Add constraint to enforce valid roles (optional, if using String in DB)
-- Note: If using Enum in Java with @Enumerated(EnumType.STRING), JPA handles this

-- Step 4: Update default value for new users
ALTER TABLE users ALTER COLUMN role SET DEFAULT 'FREE';

-- Step 5: Create seed admin user (if not exists)
-- IMPORTANT: Change email and password hash before production deployment
INSERT INTO users (id, email, password_hash, name, role, is_active, email_verified, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'admin@nihongo.local',
    '$2a$10$dummyhash', -- TODO: Replace with actual bcrypt hash of secure password
    'System Administrator',
    'ADMIN',
    true,
    true,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE role = 'ADMIN'
);

-- Step 6: Create audit log for role changes (future enhancement)
-- CREATE TABLE IF NOT EXISTS user_role_audit (
--     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
--     user_id UUID NOT NULL REFERENCES users(id),
--     changed_by_user_id UUID REFERENCES users(id),
--     old_role VARCHAR(20),
--     new_role VARCHAR(20) NOT NULL,
--     changed_at TIMESTAMP DEFAULT NOW(),
--     reason TEXT
-- );

-- Verification queries (run after migration):
-- SELECT role, COUNT(*) as count FROM users GROUP BY role ORDER BY role;
-- SELECT * FROM users WHERE role = 'ADMIN'; -- Should have at least 1 admin
