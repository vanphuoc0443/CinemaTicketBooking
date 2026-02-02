DROP DATABASE IF EXISTS cinema_booking;
CREATE DATABASE cinema_booking 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE cinema_booking;

-- ====================
-- BẢNG MOVIES
-- ====================
CREATE TABLE movies (
    movie_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    duration INT NOT NULL COMMENT 'Thời lượng phim tính bằng phút',
    description TEXT,
    poster_url VARCHAR(500),
    release_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_title (title),
    INDEX idx_genre (genre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================
-- BẢNG SHOWTIMES (ĐÃ FIX: Bỏ CURDATE() trong CHECK constraint)
-- ====================
CREATE TABLE showtimes (
    showtime_id INT PRIMARY KEY AUTO_INCREMENT,
    movie_id INT NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    room_number INT NOT NULL,
    total_seats INT DEFAULT 80,
    available_seats INT DEFAULT 80,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    
    -- Constraint: Không cho 2 suất chiếu cùng phòng, cùng thời gian
    UNIQUE KEY unique_showtime (room_number, show_date, show_time),
    
    INDEX idx_movie (movie_id),
    INDEX idx_date (show_date),
    INDEX idx_room (room_number)
    
    -- NOTE: Bỏ CHECK constraint với CURDATE() vì MySQL không hỗ trợ
    -- Sẽ validate ở application layer (Java)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================
-- BẢNG SEATS
-- ====================
CREATE TABLE seats (
    seat_id INT PRIMARY KEY AUTO_INCREMENT,
    showtime_id INT NOT NULL,
    seat_number VARCHAR(10) NOT NULL COMMENT 'Ví dụ: A1, B5, H10',
    seat_type ENUM('STANDARD', 'VIP', 'COUPLE') NOT NULL DEFAULT 'STANDARD',
    status ENUM('AVAILABLE', 'RESERVED', 'BOOKED') NOT NULL DEFAULT 'AVAILABLE',
    price DECIMAL(10,2) NOT NULL,
    version INT DEFAULT 0 COMMENT 'Optimistic locking version',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (showtime_id) REFERENCES showtimes(showtime_id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    
    -- Constraint: Mỗi suất chiếu không có ghế trùng số
    UNIQUE KEY unique_seat (showtime_id, seat_number),
    
    INDEX idx_showtime (showtime_id),
    INDEX idx_status (status),
    INDEX idx_showtime_status (showtime_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================
-- BẢNG CUSTOMERS
-- ====================
CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    date_of_birth DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================
-- BẢNG BOOKINGS
-- ====================
CREATE TABLE bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    showtime_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    cancellation_reason TEXT,
    
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    FOREIGN KEY (showtime_id) REFERENCES showtimes(showtime_id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    
    INDEX idx_customer (customer_id),
    INDEX idx_showtime (showtime_id),
    INDEX idx_status (status),
    INDEX idx_booking_time (booking_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================
-- BẢNG BOOKING_SEATS (Many-to-Many)
-- ====================
CREATE TABLE booking_seats (
    booking_id INT NOT NULL,
    seat_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (booking_id, seat_id),
    
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    
    INDEX idx_booking (booking_id),
    INDEX idx_seat (seat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================
-- BẢNG PAYMENTS
-- ====================
CREATE TABLE payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'E_WALLET', 'BANK_TRANSFER') NOT NULL,
    transaction_id VARCHAR(100) UNIQUE,
    payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    payment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    
    INDEX idx_booking (booking_id),
    INDEX idx_transaction (transaction_id),
    INDEX idx_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================
-- TRIGGERS (Tự động cập nhật available_seats)
-- ====================

DELIMITER //

-- Trigger: Giảm available_seats khi đặt ghế
CREATE TRIGGER after_seat_booked
AFTER UPDATE ON seats
FOR EACH ROW
BEGIN
    IF OLD.status = 'AVAILABLE' AND NEW.status IN ('RESERVED', 'BOOKED') THEN
        UPDATE showtimes 
        SET available_seats = available_seats - 1
        WHERE showtime_id = NEW.showtime_id;
    END IF;
END//

-- Trigger: Tăng available_seats khi hủy ghế
CREATE TRIGGER after_seat_released
AFTER UPDATE ON seats
FOR EACH ROW
BEGIN
    IF OLD.status IN ('RESERVED', 'BOOKED') AND NEW.status = 'AVAILABLE' THEN
        UPDATE showtimes 
        SET available_seats = available_seats + 1
        WHERE showtime_id = NEW.showtime_id;
    END IF;
END//

DELIMITER ;

-- ====================
-- STORED PROCEDURES
-- ====================

DELIMITER //

-- Procedure: Tạo ghế tự động cho suất chiếu mới
CREATE PROCEDURE create_seats_for_showtime(IN p_showtime_id INT)
BEGIN
    DECLARE v_row_char CHAR(1);
    DECLARE v_seat_num INT;
    DECLARE v_row_count INT DEFAULT 0;
    
    -- Xóa ghế cũ nếu có
    DELETE FROM seats WHERE showtime_id = p_showtime_id;
    
    -- Tạo ghế Standard (Hàng A-D, 40 ghế)
    WHILE v_row_count < 4 DO
        SET v_row_char = CHAR(65 + v_row_count); -- A, B, C, D
        SET v_seat_num = 1;
        
        WHILE v_seat_num <= 10 DO
            INSERT INTO seats (showtime_id, seat_number, seat_type, status, price, version)
            VALUES (p_showtime_id, CONCAT(v_row_char, v_seat_num), 'STANDARD', 'AVAILABLE', 50000, 0);
            
            SET v_seat_num = v_seat_num + 1;
        END WHILE;
        
        SET v_row_count = v_row_count + 1;
    END WHILE;
    
    -- Tạo ghế VIP (Hàng E-G, 30 ghế)
    WHILE v_row_count < 7 DO
        SET v_row_char = CHAR(65 + v_row_count); -- E, F, G
        SET v_seat_num = 1;
        
        WHILE v_seat_num <= 10 DO
            INSERT INTO seats (showtime_id, seat_number, seat_type, status, price, version)
            VALUES (p_showtime_id, CONCAT(v_row_char, v_seat_num), 'VIP', 'AVAILABLE', 100000, 0);
            
            SET v_seat_num = v_seat_num + 1;
        END WHILE;
        
        SET v_row_count = v_row_count + 1;
    END WHILE;
    
    -- Tạo ghế Couple (Hàng H, 10 ghế)
    SET v_row_char = 'H';
    SET v_seat_num = 1;
    
    WHILE v_seat_num <= 10 DO
        INSERT INTO seats (showtime_id, seat_number, seat_type, status, price, version)
        VALUES (p_showtime_id, CONCAT(v_row_char, v_seat_num), 'COUPLE', 'AVAILABLE', 150000, 0);
        
        SET v_seat_num = v_seat_num + 1;
    END WHILE;
    
END//

DELIMITER ;

-- ====================
-- INSERT SAMPLE DATA
-- ====================

-- Insert Movies
INSERT INTO movies (title, genre, duration, description, release_date) VALUES
('Avatar: The Way of Water', 'Sci-Fi', 192, 'Jake Sully lives with his new family on the ocean planet of Pandora.', '2022-12-16'),
('Avengers: Endgame', 'Action', 181, 'After the devastating events, the Avengers assemble once more.', '2019-04-26'),
('Spider-Man: No Way Home', 'Action', 148, 'Peter Parker seeks help from Doctor Strange.', '2021-12-17'),
('The Batman', 'Action', 176, 'Batman ventures into Gotham underworld.', '2022-03-04'),
('Top Gun: Maverick', 'Action', 130, 'After thirty years, Maverick is still pushing the envelope.', '2022-05-27');

-- Insert Showtimes
INSERT INTO showtimes (movie_id, show_date, show_time, room_number) VALUES
(1, '2026-01-15', '10:00:00', 1),
(1, '2026-01-15', '14:00:00', 1),
(1, '2026-01-15', '18:00:00', 2),
(2, '2026-01-15', '11:00:00', 3),
(2, '2026-01-15', '15:00:00', 3),
(3, '2026-01-16', '09:00:00', 1),
(3, '2026-01-16', '13:00:00', 2),
(4, '2026-01-16', '16:00:00', 1),
(5, '2026-01-16', '19:00:00', 3);

-- Tạo ghế cho tất cả suất chiếu
CALL create_seats_for_showtime(1);
CALL create_seats_for_showtime(2);
CALL create_seats_for_showtime(3);
CALL create_seats_for_showtime(4);
CALL create_seats_for_showtime(5);
CALL create_seats_for_showtime(6);
CALL create_seats_for_showtime(7);
CALL create_seats_for_showtime(8);
CALL create_seats_for_showtime(9);

-- Insert Sample Customers
INSERT INTO customers (name, email, phone, date_of_birth) VALUES
('Nguyễn Văn An', 'nguyenvanan@gmail.com', '0901234567', '1995-05-15'),
('Trần Thị Bình', 'tranthibinh@gmail.com', '0912345678', '1998-08-20'),
('Lê Văn Cường', 'levancuong@gmail.com', '0923456789', '1992-03-10'),
('Phạm Đức Tài', 'taiabc@gmail.com', '0923436789', '1999-03-10');

-- Insert Sample Booking
INSERT INTO bookings (customer_id, showtime_id, total_amount, status, confirmed_at) VALUES
(1, 1, 100000, 'CONFIRMED', NOW());

-- Link booking với ghế
INSERT INTO booking_seats (booking_id, seat_id) VALUES
(1, 1), -- Ghế A1
(1, 2); -- Ghế A2

-- Update seat status
UPDATE seats SET status = 'BOOKED' WHERE seat_id IN (1, 2);

-- Insert Payment
INSERT INTO payments (booking_id, amount, payment_method, transaction_id, payment_status) VALUES
(1, 100000, 'CREDIT_CARD', 'TXN001', 'COMPLETED');

-- ====================
-- VIEWS (Truy vấn tiện lợi)
-- ====================

-- View: Thông tin đầy đủ của booking
CREATE VIEW booking_details AS
SELECT 
    b.booking_id,
    c.name AS customer_name,
    c.email AS customer_email,
    m.title AS movie_title,
    s.show_date,
    s.show_time,
    s.room_number,
    GROUP_CONCAT(st.seat_number ORDER BY st.seat_number SEPARATOR ', ') AS seats,
    b.total_amount,
    b.status,
    b.booking_time
FROM bookings b
JOIN customers c ON b.customer_id = c.customer_id
JOIN showtimes s ON b.showtime_id = s.showtime_id
JOIN movies m ON s.movie_id = m.movie_id
JOIN booking_seats bs ON b.booking_id = bs.booking_id
JOIN seats st ON bs.seat_id = st.seat_id
GROUP BY b.booking_id, c.name, c.email, m.title, s.show_date, s.show_time, 
         s.room_number, b.total_amount, b.status, b.booking_time;

-- View: Tổng quan suất chiếu
CREATE VIEW showtime_overview AS
SELECT 
    s.showtime_id,
    m.title AS movie_title,
    m.duration,
    s.show_date,
    s.show_time,
    s.room_number,
    s.total_seats,
    s.available_seats,
    (s.total_seats - s.available_seats) AS booked_seats,
    ROUND((s.total_seats - s.available_seats) * 100.0 / s.total_seats, 2) AS occupancy_rate
FROM showtimes s
JOIN movies m ON s.movie_id = m.movie_id;

-- ====================
-- VERIFY DATABASE
-- ====================

-- Kiểm tra tables
SHOW TABLES;

-- Kiểm tra dữ liệu
SELECT 'Movies:' AS Info;
SELECT * FROM movies;

SELECT 'Showtimes:' AS Info;
SELECT * FROM showtime_overview;

SELECT 'Sample Seats from Showtime 1:' AS Info;
SELECT seat_number, seat_type, status, price 
FROM seats 
WHERE showtime_id = 1
ORDER BY seat_number
LIMIT 20;

SELECT 'Bookings:' AS Info;
SELECT * FROM booking_details;

SELECT 'Database created successfully!' AS Status;

SELECT * FROM movies WHERE title = 'ROLLBACK_TEST';
