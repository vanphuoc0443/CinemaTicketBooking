-- ============================================
-- ROLLBACK SCRIPT - Undo Authentication & Seat Locking Migration
-- Use this if you need to revert the changes
-- ============================================

USE cinema_booking;

SET FOREIGN_KEY_CHECKS = 0;

SELECT '========================================' AS '';
SELECT 'STARTING ROLLBACK...' AS '';
SELECT '========================================' AS '';

-- Drop event scheduler
SELECT 'Dropping event scheduler...' AS Status;
DROP EVENT IF EXISTS auto_cleanup_seat_locks;

-- Drop triggers
SELECT 'Dropping triggers...' AS Status;
DROP TRIGGER IF EXISTS log_booking_status_change;
DROP TRIGGER IF EXISTS log_booking_creation;

-- Drop stored procedures
SELECT 'Dropping stored procedures...' AS Status;
DROP PROCEDURE IF EXISTS cleanup_expired_locks;
DROP PROCEDURE IF EXISTS get_available_seats;
DROP PROCEDURE IF EXISTS lock_seats;

-- Drop views
SELECT 'Dropping views...' AS Status;
DROP VIEW IF EXISTS customer_booking_summary;
DROP VIEW IF EXISTS showtime_availability_extended;

-- Drop new tables
SELECT 'Dropping new tables...' AS Status;
DROP TABLE IF EXISTS booking_history;
DROP TABLE IF EXISTS seat_locks;
DROP TABLE IF EXISTS user_sessions;

-- Remove authentication columns from customers
SELECT 'Removing authentication columns from customers table...' AS Status;
ALTER TABLE customers DROP COLUMN IF EXISTS password_hash;
ALTER TABLE customers DROP COLUMN IF EXISTS salt;
ALTER TABLE customers DROP COLUMN IF EXISTS session_token;
ALTER TABLE customers DROP COLUMN IF EXISTS last_login;
ALTER TABLE customers DROP COLUMN IF EXISTS is_active;

-- Drop indexes
SELECT 'Dropping indexes...' AS Status;
-- Note: Indexes will be dropped automatically with columns

SET FOREIGN_KEY_CHECKS = 1;

SELECT '========================================' AS '';
SELECT 'ROLLBACK COMPLETED!' AS '';
SELECT 'Database reverted to original state' AS '';
SELECT '========================================' AS '';

-- Verify
SELECT 'Verifying rollback...' AS Status;
SHOW COLUMNS FROM customers;
SHOW TABLES LIKE '%lock%';
SHOW TABLES LIKE '%session%';
SHOW TABLES LIKE '%history%';
