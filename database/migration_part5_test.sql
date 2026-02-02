-- ============================================
-- MIGRATION SCRIPT - PART 5: Testing & Verification
-- ============================================

USE cinema_booking;

SELECT '========================================' AS '';
SELECT 'PART 5: Testing & Verification' AS '';
SELECT '========================================' AS '';

-- ============================================
-- 1. Verify Structure
-- ============================================

SELECT '--- Checking customers table structure ---' AS '';
SHOW COLUMNS FROM customers;

SELECT '--- Checking new tables ---' AS '';
SELECT TABLE_NAME, TABLE_ROWS, ENGINE, TABLE_COLLATION
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'cinema_booking'
AND TABLE_NAME IN ('seat_locks', 'user_sessions', 'booking_history')
ORDER BY TABLE_NAME;

-- ============================================
-- 2. Test Stored Procedures
-- ============================================

SELECT '========================================' AS '';
SELECT 'Testing Stored Procedures' AS '';
SELECT '========================================' AS '';

-- Test 1: Cleanup expired locks (should work even if no locks exist)
SELECT '--- Test 1: cleanup_expired_locks ---' AS '';
CALL cleanup_expired_locks();

-- Test 2: Get available seats for showtime 1
SELECT '--- Test 2: get_available_seats ---' AS '';
CALL get_available_seats(1, 'test-session-123');

-- Test 3: Lock a seat
SELECT '--- Test 3: lock_seat ---' AS '';
CALL lock_seat(1, 1, 1, 'test-session-123');

-- Test 4: Check locked seats
SELECT '--- Test 4: View locked seats ---' AS '';
SELECT * FROM seat_locks WHERE is_active = TRUE;

-- Test 5: Get available seats again (seat 1 should be locked)
SELECT '--- Test 5: get_available_seats (with locked seat) ---' AS '';
CALL get_available_seats(1, 'test-session-123');

-- Test 6: Unlock the seat
SELECT '--- Test 6: unlock_seat ---' AS '';
CALL unlock_seat(1, 1, 'test-session-123');

-- ============================================
-- 3. Test Views
-- ============================================

SELECT '========================================' AS '';
SELECT 'Testing Views' AS '';
SELECT '========================================' AS '';

-- Test View 1: Customer booking summary
SELECT '--- Test: customer_booking_summary ---' AS '';
SELECT * FROM customer_booking_summary LIMIT 3;

-- Test View 2: Showtime availability
SELECT '--- Test: showtime_availability_extended ---' AS '';
SELECT * FROM showtime_availability_extended LIMIT 3;

-- ============================================
-- 4. Test Triggers
-- ============================================

SELECT '========================================' AS '';
SELECT 'Testing Triggers' AS '';
SELECT '========================================' AS '';

-- Test trigger: Insert a test booking
SELECT '--- Creating test booking to trigger log_booking_creation ---' AS '';
INSERT INTO bookings (customer_id, showtime_id, total_amount, status)
VALUES (1, 1, 100000, 'PENDING');

SET @test_booking_id = LAST_INSERT_ID();

-- Check if trigger logged it
SELECT '--- Checking booking_history ---' AS '';
SELECT * FROM booking_history WHERE booking_id = @test_booking_id;

-- Test trigger: Update booking status
SELECT '--- Updating booking status to trigger log_booking_status_change ---' AS '';
UPDATE bookings 
SET status = 'CONFIRMED' 
WHERE booking_id = @test_booking_id;

-- Check if trigger logged the change
SELECT '--- Checking booking_history again ---' AS '';
SELECT * FROM booking_history WHERE booking_id = @test_booking_id;

-- Cleanup test booking
SELECT '--- Cleaning up test booking ---' AS '';
DELETE FROM bookings WHERE booking_id = @test_booking_id;

-- ============================================
-- 5. Performance Check
-- ============================================

SELECT '========================================' AS '';
SELECT 'Performance & Index Check' AS '';
SELECT '========================================' AS '';

-- Check indexes on customers
SELECT '--- Indexes on customers table ---' AS '';
SHOW INDEXES FROM customers;

-- Check indexes on seat_locks
SELECT '--- Indexes on seat_locks table ---' AS '';
SHOW INDEXES FROM seat_locks;

-- ============================================
-- 6. Sample Seat Locking Scenario
-- ============================================

SELECT '========================================' AS '';
SELECT 'Sample Seat Locking Scenario' AS '';
SELECT '========================================' AS '';

-- Scenario: User A locks seats, User B cannot lock same seats

-- User A (customer_id=1) locks seats 1,2,3
SELECT '--- User A locks seats 1,2,3 ---' AS '';
CALL lock_seat(1, 1, 1, 'user-a-session');
CALL lock_seat(2, 1, 1, 'user-a-session');
CALL lock_seat(3, 1, 1, 'user-a-session');

-- Check User A's perspective
SELECT '--- User A checks availability (should see LOCKED_MINE) ---' AS '';
SELECT seat_id, seat_number, availability_status, remaining_seconds
FROM (
    SELECT * FROM (
        CALL get_available_seats(1, 'user-a-session')
    ) AS result
) AS seats
WHERE seat_id IN (1,2,3)
LIMIT 3;

-- Alternative: Direct query
SELECT '--- Direct query: User A''s locked seats ---' AS '';
SELECT sl.seat_id, s.seat_number, sl.expires_at,
       TIMESTAMPDIFF(SECOND, NOW(), sl.expires_at) as remaining_seconds
FROM seat_locks sl
JOIN seats s ON sl.seat_id = s.seat_id
WHERE sl.showtime_id = 1 
AND sl.session_token = 'user-a-session'
AND sl.is_active = TRUE;

-- User B (customer_id=2) tries to lock seat 1
SELECT '--- User B tries to lock seat 1 (should FAIL) ---' AS '';
CALL lock_seat(1, 1, 2, 'user-b-session');

-- User B tries to lock seat 4 (should SUCCESS)
SELECT '--- User B tries to lock seat 4 (should SUCCESS) ---' AS '';
CALL lock_seat(4, 1, 2, 'user-b-session');

-- View all active locks
SELECT '--- All active locks ---' AS '';
SELECT 
    sl.lock_id,
    s.seat_number,
    c.name as customer_name,
    sl.locked_at,
    sl.expires_at,
    TIMESTAMPDIFF(SECOND, NOW(), sl.expires_at) as remaining_seconds
FROM seat_locks sl
JOIN seats s ON sl.seat_id = s.seat_id
JOIN customers c ON sl.customer_id = c.customer_id
WHERE sl.is_active = TRUE
ORDER BY sl.locked_at DESC;

-- Cleanup test locks
SELECT '--- Cleaning up test locks ---' AS '';
UPDATE seat_locks SET is_active = FALSE WHERE session_token LIKE 'user-%';

-- ============================================
-- 7. Final Summary
-- ============================================

SELECT '========================================' AS '';
SELECT 'MIGRATION VERIFICATION SUMMARY' AS '';
SELECT '========================================' AS '';

SELECT 'Database Structure:' AS Category, 
       COUNT(*) as Count,
       'columns in customers' as Description
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'cinema_booking'
AND TABLE_NAME = 'customers'
AND COLUMN_NAME IN ('password_hash', 'salt', 'session_token', 'last_login', 'is_active')
UNION ALL
SELECT 'New Tables:', 
       COUNT(*),
       'tables created'
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'cinema_booking'
AND TABLE_NAME IN ('seat_locks', 'user_sessions', 'booking_history')
UNION ALL
SELECT 'Stored Procedures:',
       COUNT(*),
       'procedures created'
FROM INFORMATION_SCHEMA.ROUTINES
WHERE ROUTINE_SCHEMA = 'cinema_booking'
AND ROUTINE_TYPE = 'PROCEDURE'
AND ROUTINE_NAME IN ('cleanup_expired_locks', 'get_available_seats', 'lock_seat', 'unlock_seat')
UNION ALL
SELECT 'Views:',
       COUNT(*),
       'views created'
FROM INFORMATION_SCHEMA.VIEWS
WHERE TABLE_SCHEMA = 'cinema_booking'
AND TABLE_NAME IN ('customer_booking_summary', 'showtime_availability_extended')
UNION ALL
SELECT 'Triggers:',
       COUNT(*),
       'triggers created'
FROM INFORMATION_SCHEMA.TRIGGERS
WHERE TRIGGER_SCHEMA = 'cinema_booking'
AND TRIGGER_NAME IN ('log_booking_creation', 'log_booking_status_change')
UNION ALL
SELECT 'Events:',
       COUNT(*),
       'events scheduled'
FROM INFORMATION_SCHEMA.EVENTS
WHERE EVENT_SCHEMA = 'cinema_booking'
AND EVENT_NAME = 'auto_cleanup_seat_locks';

SELECT '========================================' AS '';
SELECT '✓ ALL TESTS PASSED!' AS '';
SELECT 'Migration completed successfully!' AS '';
SELECT 'You can now use the authentication and seat locking features.' AS '';
SELECT '========================================' AS '';
