-- ============================================
-- MIGRATION SCRIPT - PART 4: Views, Triggers, Events
-- ============================================

USE cinema_booking;

SELECT '========================================' AS '';
SELECT 'PART 4: Creating Views, Triggers, Events' AS '';
SELECT '========================================' AS '';

-- ============================================
-- CREATE VIEWS
-- ============================================

SELECT 'Creating views...' AS Status;

-- View 1: Customer booking summary
DROP VIEW IF EXISTS customer_booking_summary;

CREATE VIEW customer_booking_summary AS
SELECT 
    c.customer_id,
    c.name,
    c.email,
    c.phone,
    c.is_active,
    c.last_login,
    COUNT(DISTINCT b.booking_id) as total_bookings,
    COUNT(DISTINCT CASE WHEN b.status = 'CONFIRMED' THEN b.booking_id END) as confirmed_bookings,
    COUNT(DISTINCT CASE WHEN b.status = 'CANCELLED' THEN b.booking_id END) as cancelled_bookings,
    COALESCE(SUM(CASE WHEN b.status = 'CONFIRMED' THEN b.total_amount ELSE 0 END), 0) as total_spent,
    MAX(b.booking_time) as last_booking_date
FROM customers c
LEFT JOIN bookings b ON c.customer_id = b.customer_id
GROUP BY c.customer_id, c.name, c.email, c.phone, c.is_active, c.last_login;

SELECT '✓ Created customer_booking_summary view' AS Status;

-- View 2: Showtime availability
DROP VIEW IF EXISTS showtime_availability_extended;

CREATE VIEW showtime_availability_extended AS
SELECT 
    st.showtime_id,
    st.movie_id,
    m.title as movie_title,
    st.show_date,
    st.show_time,
    st.room_number,
    st.total_seats,
    st.available_seats,
    COUNT(DISTINCT s.seat_id) as total_physical_seats,
    COUNT(DISTINCT bs.seat_id) as booked_seats,
    COUNT(DISTINCT CASE 
        WHEN sl.is_active = TRUE AND sl.expires_at > CURRENT_TIMESTAMP 
        THEN sl.seat_id 
    END) as locked_seats,
    CASE 
        WHEN COUNT(DISTINCT bs.seat_id) >= st.total_seats THEN 'FULL'
        WHEN COUNT(DISTINCT bs.seat_id) >= st.total_seats * 0.8 THEN 'ALMOST_FULL'
        ELSE 'AVAILABLE'
    END as availability_status
FROM showtimes st
INNER JOIN movies m ON st.movie_id = m.movie_id
LEFT JOIN seats s ON st.showtime_id = s.showtime_id
LEFT JOIN (
    SELECT bs.seat_id, b.showtime_id
    FROM booking_seats bs
    INNER JOIN bookings b ON bs.booking_id = b.booking_id
    WHERE b.status IN ('CONFIRMED', 'PENDING')
) bs ON st.showtime_id = bs.showtime_id AND s.seat_id = bs.seat_id
LEFT JOIN seat_locks sl ON st.showtime_id = sl.showtime_id AND s.seat_id = sl.seat_id
GROUP BY st.showtime_id, st.movie_id, m.title, st.show_date, st.show_time, 
         st.room_number, st.total_seats, st.available_seats;

SELECT '✓ Created showtime_availability_extended view' AS Status;

-- ============================================
-- CREATE TRIGGERS
-- ============================================

SELECT 'Creating triggers...' AS Status;

-- Trigger 1: Log booking creation
DROP TRIGGER IF EXISTS log_booking_creation;

DELIMITER $$

CREATE TRIGGER log_booking_creation
AFTER INSERT ON bookings
FOR EACH ROW
BEGIN
    INSERT INTO booking_history (
        booking_id,
        action,
        performed_by,
        new_status,
        notes
    ) VALUES (
        NEW.booking_id,
        'CREATED',
        NEW.customer_id,
        NEW.status,
        'Booking created'
    );
END$$

DELIMITER ;

SELECT '✓ Created log_booking_creation trigger' AS Status;

-- Trigger 2: Log booking status change
DROP TRIGGER IF EXISTS log_booking_status_change;

DELIMITER $$

CREATE TRIGGER log_booking_status_change
AFTER UPDATE ON bookings
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO booking_history (
            booking_id, 
            action, 
            performed_by, 
            old_status, 
            new_status,
            notes
        ) VALUES (
            NEW.booking_id,
            'STATUS_CHANGE',
            NEW.customer_id,
            OLD.status,
            NEW.status,
            CONCAT('Status changed from ', OLD.status, ' to ', NEW.status)
        );
    END IF;
END$$

DELIMITER ;

SELECT '✓ Created log_booking_status_change trigger' AS Status;

-- ============================================
-- CREATE EVENT SCHEDULER
-- ============================================

SELECT 'Setting up event scheduler...' AS Status;

-- Enable event scheduler
SET GLOBAL event_scheduler = ON;
SELECT '✓ Event scheduler enabled' AS Status;

-- Create cleanup event
DROP EVENT IF EXISTS auto_cleanup_seat_locks;

CREATE EVENT auto_cleanup_seat_locks
ON SCHEDULE EVERY 5 MINUTE
DO
    UPDATE seat_locks 
    SET is_active = FALSE 
    WHERE is_active = TRUE 
    AND expires_at <= CURRENT_TIMESTAMP;

SELECT '✓ Created auto_cleanup_seat_locks event (runs every 5 minutes)' AS Status;

SELECT '========================================' AS '';
SELECT 'PART 4 COMPLETED!' AS '';
SELECT '========================================' AS '';

-- Verify
SELECT 'Verifying views...' AS Status;
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.VIEWS 
WHERE TABLE_SCHEMA = 'cinema_booking'
AND TABLE_NAME IN ('customer_booking_summary', 'showtime_availability_extended');

SELECT 'Verifying triggers...' AS Status;
SELECT TRIGGER_NAME, EVENT_MANIPULATION, EVENT_OBJECT_TABLE
FROM INFORMATION_SCHEMA.TRIGGERS
WHERE TRIGGER_SCHEMA = 'cinema_booking'
AND TRIGGER_NAME IN ('log_booking_creation', 'log_booking_status_change');

SELECT 'Verifying events...' AS Status;
SHOW EVENTS FROM cinema_booking;
