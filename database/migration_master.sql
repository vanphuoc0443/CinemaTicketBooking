-- ============================================
-- MASTER MIGRATION SCRIPT
-- Chạy script này để thực hiện TOÀN BỘ migration
-- Hoặc chạy từng part riêng lẻ nếu muốn kiểm soát từng bước
-- ============================================

USE cinema_booking;

SELECT '╔════════════════════════════════════════╗' AS '';
SELECT '║   CINEMA BOOKING SYSTEM MIGRATION     ║' AS '';
SELECT '║   Adding Authentication & Seat Lock   ║' AS '';
SELECT '╚════════════════════════════════════════╝' AS '';
SELECT '' AS '';

-- ============================================
-- CÁCH SỬ DỤNG:
-- ============================================
-- Option 1: Chạy file này (master script)
--   mysql -u root -p cinema_booking < migration_master.sql
--
-- Option 2: Chạy từng part (khuyến nghị cho debug)
--   mysql -u root -p cinema_booking < migration_part1.sql
--   mysql -u root -p cinema_booking < migration_part2.sql
--   mysql -u root -p cinema_booking < migration_part3.sql
--   mysql -u root -p cinema_booking < migration_part4.sql
--   mysql -u root -p cinema_booking < migration_part5_test.sql
--
-- Option 3: Copy/paste từng phần vào MySQL Workbench
--   và execute từng phần một
-- ============================================

SELECT 'Starting migration in 3 seconds...' AS Status;
SELECT SLEEP(1);
SELECT '3...' AS '';
SELECT SLEEP(1);
SELECT '2...' AS '';
SELECT SLEEP(1);
SELECT '1...' AS '';

-- ============================================
-- PART 1: Add Authentication Columns
-- ============================================

SOURCE migration_part1.sql;

-- ============================================
-- PART 2: Create New Tables
-- ============================================

SOURCE migration_part2.sql;

-- ============================================
-- PART 3: Create Stored Procedures
-- ============================================

SOURCE migration_part3.sql;

-- ============================================
-- PART 4: Create Views, Triggers, Events
-- ============================================

SOURCE migration_part4.sql;

-- ============================================
-- PART 5: Testing & Verification
-- ============================================

SOURCE migration_part5_test.sql;

-- ============================================
-- FINAL MESSAGE
-- ============================================

SELECT '' AS '';
SELECT '╔════════════════════════════════════════╗' AS '';
SELECT '║     MIGRATION COMPLETED! ✓             ║' AS '';
SELECT '╚════════════════════════════════════════╝' AS '';
SELECT '' AS '';
SELECT 'Next steps:' AS '';
SELECT '1. Add Java files to your project' AS '';
SELECT '2. Create REST API controllers' AS '';
SELECT '3. Test authentication with frontend' AS '';
SELECT '4. Test seat locking functionality' AS '';
SELECT '' AS '';
SELECT 'See QUICK_START_GUIDE.md for detailed instructions' AS '';
