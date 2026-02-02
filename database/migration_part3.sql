-- ============================================
-- MIGRATION SCRIPT - PART 3: Stored Procedures
-- ============================================

USE cinema_booking;

SELECT '========================================' AS '';
SELECT 'PART 3: Creating Stored Procedures' AS '';
SELECT '========================================' AS '';

-- ============================================
-- Procedure 1: cleanup_expired_locks
-- ============================================

DROP PROCEDURE IF EXISTS cleanup_expired_locks;

DELIMITER $$

CREATE PROCEDURE cleanup_expired_locks()
BEGIN
    DECLARE affected_rows INT;
    
    UPDATE seat_locks 
    SET is_active = FALSE 
    WHERE is_active = TRUE 
    AND expires_at <= CURRENT_TIMESTAMP;
    
    SET affected_rows = ROW_COUNT();
    
    SELECT CONCAT('Cleaned up ', affected_rows, ' expired locks') AS result;
END$$

DELIMITER ;

SELECT '✓ Created cleanup_expired_locks procedure' AS Status;

-- ============================================
-- Procedure 2: get_available_seats
-- ============================================

DROP PROCEDURE IF EXISTS get_available_seats;

DELIMITER $$

CREATE PROCEDURE get_available_seats(
    IN p_showtime_id INT, 
    IN p_session_token VARCHAR(255)
)
BEGIN
    -- First cleanup expired locks
    UPDATE seat_locks 
    SET is_active = FALSE 
    WHERE is_active = TRUE 
    AND expires_at <= CURRENT_TIMESTAMP
    AND showtime_id = p_showtime_id;
    
    -- Then return seat status
    SELECT 
        s.seat_id,
        s.seat_number,
        s.seat_type,
        s.price,
        s.status as seat_status,
        CASE
            WHEN bs.seat_id IS NOT NULL THEN 'BOOKED'
            WHEN sl.session_token = p_session_token AND sl.is_active = TRUE THEN 'LOCKED_MINE'
            WHEN sl.seat_id IS NOT NULL AND sl.is_active = TRUE THEN 'LOCKED_OTHERS'
            WHEN s.status = 'AVAILABLE' THEN 'AVAILABLE'
            ELSE s.status
        END as availability_status,
        sl.expires_at as lock_expires_at,
        TIMESTAMPDIFF(SECOND, CURRENT_TIMESTAMP, sl.expires_at) as remaining_seconds
    FROM seats s
    LEFT JOIN (
        SELECT DISTINCT bs.seat_id
        FROM booking_seats bs
        INNER JOIN bookings b ON bs.booking_id = b.booking_id
        WHERE b.showtime_id = p_showtime_id
        AND b.status IN ('CONFIRMED', 'PENDING')
    ) bs ON s.seat_id = bs.seat_id
    LEFT JOIN (
        SELECT seat_id, session_token, expires_at
        FROM seat_locks
        WHERE showtime_id = p_showtime_id
        AND is_active = TRUE
        AND expires_at > CURRENT_TIMESTAMP
    ) sl ON s.seat_id = sl.seat_id
    WHERE s.showtime_id = p_showtime_id
    ORDER BY s.seat_number;
END$$

DELIMITER ;

SELECT '✓ Created get_available_seats procedure' AS Status;

-- ============================================
-- Procedure 3: Simple lock_seat (single seat)
-- ============================================

DROP PROCEDURE IF EXISTS lock_seat;

DELIMITER $$

CREATE PROCEDURE lock_seat(
    IN p_seat_id INT,
    IN p_showtime_id INT,
    IN p_customer_id INT,
    IN p_session_token VARCHAR(255)
)
BEGIN
    DECLARE lock_duration INT DEFAULT 10;
    DECLARE expires TIMESTAMP;
    DECLARE is_available INT;
    
    SET expires = TIMESTAMPADD(MINUTE, lock_duration, CURRENT_TIMESTAMP);
    
    -- Cleanup first
    UPDATE seat_locks 
    SET is_active = FALSE 
    WHERE is_active = TRUE 
    AND expires_at <= CURRENT_TIMESTAMP
    AND showtime_id = p_showtime_id;
    
    -- Check if seat is available
    SELECT COUNT(*) INTO is_available
    FROM seats s
    WHERE s.seat_id = p_seat_id
    AND s.showtime_id = p_showtime_id
    AND s.status = 'AVAILABLE'
    -- Not booked
    AND NOT EXISTS (
        SELECT 1 FROM booking_seats bs
        JOIN bookings b ON bs.booking_id = b.booking_id
        WHERE bs.seat_id = p_seat_id
        AND b.showtime_id = p_showtime_id
        AND b.status IN ('CONFIRMED', 'PENDING')
    )
    -- Not locked by others
    AND NOT EXISTS (
        SELECT 1 FROM seat_locks sl
        WHERE sl.seat_id = p_seat_id
        AND sl.showtime_id = p_showtime_id
        AND sl.is_active = TRUE
        AND sl.expires_at > CURRENT_TIMESTAMP
        AND sl.customer_id != p_customer_id
    );
    
    IF is_available > 0 THEN
        -- Delete old lock from same user if exists
        DELETE FROM seat_locks
        WHERE seat_id = p_seat_id
        AND showtime_id = p_showtime_id
        AND customer_id = p_customer_id;
        
        -- Insert new lock
        INSERT INTO seat_locks (
            seat_id, showtime_id, customer_id, 
            session_token, expires_at, is_active
        ) VALUES (
            p_seat_id, p_showtime_id, p_customer_id,
            p_session_token, expires, TRUE
        );
        
        SELECT 'SUCCESS' as status, 
               'Seat locked successfully' as message,
               expires as expires_at,
               lock_duration * 60 as remaining_seconds;
    ELSE
        SELECT 'FAILED' as status,
               'Seat is not available' as message,
               NULL as expires_at,
               0 as remaining_seconds;
    END IF;
END$$

DELIMITER ;

SELECT '✓ Created lock_seat procedure' AS Status;

-- ============================================
-- Procedure 4: unlock_seat
-- ============================================

DROP PROCEDURE IF EXISTS unlock_seat;

DELIMITER $$

CREATE PROCEDURE unlock_seat(
    IN p_seat_id INT,
    IN p_showtime_id INT,
    IN p_session_token VARCHAR(255)
)
BEGIN
    UPDATE seat_locks
    SET is_active = FALSE
    WHERE seat_id = p_seat_id
    AND showtime_id = p_showtime_id
    AND session_token = p_session_token
    AND is_active = TRUE;
    
    IF ROW_COUNT() > 0 THEN
        SELECT 'SUCCESS' as status, 'Seat unlocked' as message;
    ELSE
        SELECT 'FAILED' as status, 'No active lock found' as message;
    END IF;
END$$

DELIMITER ;

SELECT '✓ Created unlock_seat procedure' AS Status;

SELECT '========================================' AS '';
SELECT 'PART 3 COMPLETED!' AS '';
SELECT '========================================' AS '';

-- Verify procedures
SELECT 'Verifying procedures...' AS Status;
SHOW PROCEDURE STATUS WHERE Db = 'cinema_booking' 
AND Name IN ('cleanup_expired_locks', 'get_available_seats', 'lock_seat', 'unlock_seat');
