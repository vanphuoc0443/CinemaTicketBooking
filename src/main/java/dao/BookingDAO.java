package dao;

import model.Booking;
import model.BookingStatus;
import util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Booking
 * ✅ Đã sửa lỗi resource leak và cải thiện transaction handling
 */
public class BookingDAO {

    private static final Logger logger = LoggerFactory.getLogger(BookingDAO.class);

    /**
     * Lưu booking mới và trả về ID
     * ✅ SỬA LỖI: Sử dụng try-with-resources để tránh connection leak
     * ✅ SỬA LỖI: Thêm proper rollback handling
     */
    public int save(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (customer_id, showtime_id, total_amount, status) " +
                "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setInt(1, booking.getCustomerId());
                stmt.setInt(2, booking.getShowtimeId());
                stmt.setDouble(3, booking.getTotalAmount());
                stmt.setString(4, booking.getStatus().name());

                int result = stmt.executeUpdate();

                if (result > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int bookingId = rs.getInt(1);

                            logger.info("Created booking with ID: {}", bookingId);

                            // Lưu booking_seats
                            String seatSql = "INSERT INTO booking_seats (booking_id, seat_id) VALUES (?, ?)";
                            try (PreparedStatement seatStmt = conn.prepareStatement(seatSql)) {

                                int batchSize = 100;
                                int count = 0;

                                for (int seatId : booking.getSeatIds()) {
                                    seatStmt.setInt(1, bookingId);
                                    seatStmt.setInt(2, seatId);
                                    seatStmt.addBatch();
                                    count++;

                                    // ✅ TỐI ƯU: Execute batch mỗi 100 records
                                    if (count % batchSize == 0) {
                                        seatStmt.executeBatch();
                                        seatStmt.clearBatch();
                                    }
                                }

                                // Execute remaining
                                if (count % batchSize != 0) {
                                    seatStmt.executeBatch();
                                }
                            }

                            conn.commit();
                            logger.info("Successfully saved booking {} with {} seats", bookingId, booking.getSeatIds().size());
                            return bookingId;
                        }
                    }
                }

                logger.warn("Failed to create booking - no rows affected");
                return -1;

            } catch (SQLException e) {
                // ✅ SỬA LỖI: Rollback khi có lỗi
                if (conn != null) {
                    try {
                        conn.rollback();
                        logger.error("Rolled back transaction due to error", e);
                    } catch (SQLException rollbackEx) {
                        logger.error("Error during rollback", rollbackEx);
                    }
                }
                throw e;
            }
        } finally {
            // ✅ SỬA LỖI: Đảm bảo close connection
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.error("Error closing connection", closeEx);
                }
            }
        }
    }

    /**
     * Lấy booking theo ID
     * ✅ SỬA LỖI: Đã dùng try-with-resources đúng cách
     */
    public Booking findById(int bookingId) throws SQLException {
        String sql = "SELECT b.*, c.name as customer_name, m.title as movie_title, " +
                "s.show_date, s.show_time " +
                "FROM bookings b " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "JOIN showtimes s ON b.showtime_id = s.showtime_id " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "WHERE b.booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Booking booking = extractBookingFromResultSet(rs);

                    // Load seat IDs
                    String seatSql = "SELECT seat_id FROM booking_seats WHERE booking_id = ?";
                    try (PreparedStatement seatStmt = conn.prepareStatement(seatSql)) {
                        seatStmt.setInt(1, bookingId);

                        try (ResultSet seatRs = seatStmt.executeQuery()) {
                            while (seatRs.next()) {
                                booking.addSeatId(seatRs.getInt("seat_id"));
                            }
                        }
                    }

                    logger.debug("Found booking: {}", bookingId);
                    return booking;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding booking by id: {}", bookingId, e);
            throw e;
        }

        logger.debug("Booking not found: {}", bookingId);
        return null;
    }

    /**
     * Lấy booking theo khách hàng
     */
    public List<Booking> findByCustomer(int customerId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, c.name as customer_name, m.title as movie_title, " +
                "s.show_date, s.show_time " +
                "FROM bookings b " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "JOIN showtimes s ON b.showtime_id = s.showtime_id " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "WHERE b.customer_id = ? ORDER BY b.booking_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }

            logger.debug("Found {} bookings for customer {}", bookings.size(), customerId);
        } catch (SQLException e) {
            logger.error("Error finding bookings for customer: {}", customerId, e);
            throw e;
        }

        return bookings;
    }

    /**
     * Lấy booking theo suất chiếu
     */
    public List<Booking> findByShowtime(int showtimeId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, c.name as customer_name, m.title as movie_title, " +
                "s.show_date, s.show_time " +
                "FROM bookings b " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "JOIN showtimes s ON b.showtime_id = s.showtime_id " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "WHERE b.showtime_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showtimeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }

            logger.debug("Found {} bookings for showtime {}", bookings.size(), showtimeId);
        } catch (SQLException e) {
            logger.error("Error finding bookings for showtime: {}", showtimeId, e);
            throw e;
        }

        return bookings;
    }

    /**
     * Cập nhật trạng thái booking
     * ✅ SỬA LỖI: Proper transaction handling
     */
    public boolean updateStatus(int bookingId, BookingStatus status) throws SQLException {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status.name());
                stmt.setInt(2, bookingId);

                int result = stmt.executeUpdate();
                conn.commit();

                if (result > 0) {
                    logger.info("Updated booking {} status to {}", bookingId, status);
                    return true;
                }

                logger.warn("No booking found with id {} to update", bookingId);
                return false;

            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                        logger.error("Rolled back status update for booking {}", bookingId, e);
                    } catch (SQLException rollbackEx) {
                        logger.error("Error during rollback", rollbackEx);
                    }
                }
                throw e;
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.error("Error closing connection", closeEx);
                }
            }
        }
    }

    /**
     * Xóa booking
     * ✅ SỬA LỖI: Proper transaction handling
     */
    public boolean delete(int bookingId) throws SQLException {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, bookingId);
                int result = stmt.executeUpdate();
                conn.commit();

                if (result > 0) {
                    logger.info("Deleted booking {}", bookingId);
                    return true;
                }

                logger.warn("No booking found with id {} to delete", bookingId);
                return false;

            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                        logger.error("Rolled back delete for booking {}", bookingId, e);
                    } catch (SQLException rollbackEx) {
                        logger.error("Error during rollback", rollbackEx);
                    }
                }
                throw e;
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.error("Error closing connection", closeEx);
                }
            }
        }
    }

    /**
     * Trích xuất Booking từ ResultSet
     */
    private Booking extractBookingFromResultSet(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setCustomerId(rs.getInt("customer_id"));
        booking.setShowtimeId(rs.getInt("showtime_id"));
        booking.setTotalAmount(rs.getDouble("total_amount"));
        booking.setStatus(BookingStatus.valueOf(rs.getString("status")));
        booking.setBookingTime(rs.getTimestamp("booking_time"));
        booking.setConfirmedAt(rs.getTimestamp("confirmed_at"));
        booking.setCancelledAt(rs.getTimestamp("cancelled_at"));
        booking.setCancellationReason(rs.getString("cancellation_reason"));
        booking.setCustomerName(rs.getString("customer_name"));
        booking.setMovieTitle(rs.getString("movie_title"));
        booking.setShowDate(rs.getString("show_date"));
        booking.setShowTime(rs.getString("show_time"));
        return booking;
    }
}