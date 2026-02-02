-- ============================================
-- MIGRATION SCRIPT - PART 1: Tables & Columns
-- Chạy từng phần để dễ debug
-- ============================================

USE cinema_booking;

-- ============================================
-- PART 1: Add Authentication Columns
-- ============================================

SELECT '========================================' AS '';
SELECT 'PART 1: Adding Authentication Columns' AS '';
SELECT '========================================' AS '';

-- Add columns one by one
ALTER TABLE customers ADD COLUMN password_hash VARCHAR(255) NULL COMMENT 'Hashed password';
SELECT '✓ Added password_hash' AS Status;

ALTER TABLE customers ADD COLUMN salt VARCHAR(100) NULL COMMENT 'Password salt';
SELECT '✓ Added salt' AS Status;

ALTER TABLE customers ADD COLUMN session_token VARCHAR(255) NULL COMMENT 'Current session token';
SELECT '✓ Added session_token' AS Status;

ALTER TABLE customers ADD COLUMN last_login TIMESTAMP NULL COMMENT 'Last login time';
SELECT '✓ Added last_login' AS Status;

ALTER TABLE customers ADD COLUMN is_active BOOLEAN DEFAULT TRUE COMMENT 'Account active status';
SELECT '✓ Added is_active' AS Status;

-- Create indexes
CREATE INDEX idx_customers_session_token ON customers(session_token);
SELECT '✓ Created index on session_token' AS Status;

CREATE INDEX idx_customers_email_active ON customers(email, is_active);
SELECT '✓ Created composite index on email and is_active' AS Status;

SELECT '========================================' AS '';
SELECT 'PART 1 COMPLETED!' AS '';
SELECT '========================================' AS '';
