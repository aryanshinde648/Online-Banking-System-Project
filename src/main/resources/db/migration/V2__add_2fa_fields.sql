-- ============================================================
-- 2FA Schema Migration — Safe PostgreSQL script
-- Run this BEFORE starting the Spring Boot application
-- for the first time after the 2FA feature was added.
-- ============================================================
-- This script is idempotent: safe to run even if columns exist.

-- Step 1: Add totp_secret_key column (nullable, no default needed)
ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS totp_secret_key VARCHAR(255);

-- Step 2: Add is_2fa_enabled column with DEFAULT FALSE so existing rows get a value
ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS is_2fa_enabled BOOLEAN DEFAULT FALSE;

-- Step 3: Back-fill any remaining NULLs (belt-and-suspenders for partial migrations)
UPDATE customers
SET is_2fa_enabled = FALSE
WHERE is_2fa_enabled IS NULL;

-- ============================================================
-- Verification queries (run to confirm success):
-- ============================================================
-- SELECT column_name, data_type, column_default, is_nullable
-- FROM information_schema.columns
-- WHERE table_name = 'customers'
--   AND column_name IN ('is_2fa_enabled', 'totp_secret_key');
--
-- SELECT COUNT(*) FROM customers WHERE is_2fa_enabled IS NULL;
-- Expected result: 0
