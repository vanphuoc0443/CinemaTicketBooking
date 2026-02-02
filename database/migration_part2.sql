-- ============================================
-- MIGRATION SCRIPT - PART 2: Create New Tables
-- ============================================

USE cinema_booking;

SELECT '========================================' AS '';
SELECT 'PART 2: Creating New Tables' AS '';
SELECT '========================================' AS '';

-- ============================================
-- Create seat_locks table
-- ============================================

DROP TABLE IF EXISTS seat_locks;

CREATE TABLE seat_locks (
    lock_id INT AUTO_INCREMENT PRIMARY KEY,
    seat_id INT NOT NULL,
    showtime_id INT NOT NULL,
    customer_id INT NOT NULL,
    session_token VARCHAR(255) NOT NULL,
    locked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id) ON DELETE CASCADE,
    FOREIGN KEY (showtime_id) REFERENCES showtimes(showtime_id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    
    INDEX idx_seat_showtime (seat_id, showtime_id),
    INDEX idx_session_token (session_token),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_active (is_active),
    INDEX idx_showtime_active (showtime_id, is_active, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Table to manage temporary seat locks (10 minute hold)';

SELECT '✓ Created seat_locks table' AS Status;

-- ============================================
-- Create user_sessions table
-- ============================================

DROP TABLE IF EXISTS user_sessions;

CREATE TABLE user_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    
    INDEX idx_session_token (session_token),
    INDEX idx_customer_id (customer_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Track user sessions for security and analytics';

SELECT '✓ Created user_sessions table' AS Status;

-- ============================================
-- Create booking_history table
-- ============================================

DROP TABLE IF EXISTS booking_history;

CREATE TABLE booking_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    action VARCHAR(50) NOT NULL COMMENT 'CREATED, CONFIRMED, CANCELLED, MODIFIED',
    performed_by INT NOT NULL,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    notes TEXT,
    
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (performed_by) REFERENCES customers(customer_id),
    
    INDEX idx_booking_id (booking_id),
    INDEX idx_performed_at (performed_at),
    INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit trail for booking changes';

SELECT '✓ Created booking_history table' AS Status;

SELECT '========================================' AS '';
SELECT 'PART 2 COMPLETED!' AS '';
SELECT '========================================' AS '';

-- Verify
SELECT 'Verifying tables...' AS Status;
SELECT TABLE_NAME, TABLE_ROWS, CREATE_TIME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'cinema_booking'
AND TABLE_NAME IN ('seat_locks', 'user_sessions', 'booking_history');
