-- ============================================
-- MIGRATION: Create theaters table
-- ============================================

USE cinema_booking;

-- Create theaters table
CREATE TABLE IF NOT EXISTS theaters (
    theater_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    total_seats INT DEFAULT 80,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Phòng chiếu phim';

-- Insert default theaters
INSERT IGNORE INTO theaters (name, total_seats) VALUES
('Phòng 1', 80),
('Phòng 2', 80),
('Phòng 3', 80);

SELECT '✓ Created theaters table with default rooms' AS Status;
