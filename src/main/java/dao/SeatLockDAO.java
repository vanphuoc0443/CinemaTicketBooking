package dao;

import model.SeatLock;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO để quản lý seat locks
 */
public class SeatLockDAO {

    /**
     * Lock ghế cho user
     * @return true nếu lock thành công, false nếu ghế đã bị lock bởi người khác
     */
    public boolean lockSeat(int seatId, int showtimeId, int customerId, String sessionToken)
            throws SQLException {

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Xóa các lock đã hết hạn
            cleanupExpiredLocks(showtimeId);

            // 2. Kiểm tra ghế đã bị lock chưa
            if (isSeatLocked(seatId, showtimeId, customerId)) {
                conn.rollback();
                return false;
            }

            // 3. Tạo lock mới
            SeatLock lock = new SeatLock(seatId, showtimeId, customerId, sessionToken);

            String sql = "INSERT INTO seat_locks (seat_id, showtime_id, customer_id, " +
                    "session_token, locked_at, expires_at, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, lock.getSeatId());
                stmt.setInt(2, lock.getShowtimeId());
                stmt.setInt(3, lock.getCustomerId());
                stmt.setString(4, lock.getSessionToken());
                stmt.setTimestamp(5, lock.getLockedAt());
                stmt.setTimestamp(6, lock.getExpiresAt());
                stmt.setBoolean(7, lock.isActive());

                int result = stmt.executeUpdate();

                if (result > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            lock.setLockId(rs.getInt(1));
                        }
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Lock nhiều ghế cùng lúc
     */
    public boolean lockSeats(List<Integer> seatIds, int showtimeId, int customerId,
                             String sessionToken) throws SQLException {

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Xóa locks cũ
            cleanupExpiredLocks(showtimeId);

            // Kiểm tra tất cả ghế
            for (int seatId : seatIds) {
                if (isSeatLocked(seatId, showtimeId, customerId)) {
                    conn.rollback();
                    return false;
                }
            }

            // Lock tất cả ghế
            String sql = "INSERT INTO seat_locks (seat_id, showtime_id, customer_id, " +
                    "session_token, locked_at, expires_at, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int seatId : seatIds) {
                    SeatLock lock = new SeatLock(seatId, showtimeId, customerId, sessionToken);

                    stmt.setInt(1, lock.getSeatId());
                    stmt.setInt(2, lock.getShowtimeId());
                    stmt.setInt(3, lock.getCustomerId());
                    stmt.setString(4, lock.getSessionToken());
                    stmt.setTimestamp(5, lock.getLockedAt());
                    stmt.setTimestamp(6, lock.getExpiresAt());
                    stmt.setBoolean(7, lock.isActive());

                    stmt.addBatch();
                }

                stmt.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Unlock ghế (khi user bỏ chọn hoặc timeout)
     */
    public boolean unlockSeat(int seatId, int showtimeId, String sessionToken)
            throws SQLException {

        String sql = "UPDATE seat_locks SET is_active = FALSE " +
                "WHERE seat_id = ? AND showtime_id = ? AND session_token = ? AND is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, seatId);
            stmt.setInt(2, showtimeId);
            stmt.setString(3, sessionToken);

            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    /**
     * Unlock tất cả ghế của user (khi user rời trang)
     */
    public boolean unlockAllSeatsForSession(String sessionToken, int showtimeId)
            throws SQLException {

        String sql = "UPDATE seat_locks SET is_active = FALSE " +
                "WHERE session_token = ? AND showtime_id = ? AND is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionToken);
            stmt.setInt(2, showtimeId);

            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    /**
     * Kiểm tra ghế đã bị lock chưa (bởi người khác)
     */
    public boolean isSeatLocked(int seatId, int showtimeId, int customerId)
            throws SQLException {

        String sql = "SELECT COUNT(*) FROM seat_locks " +
                "WHERE seat_id = ? AND showtime_id = ? AND is_active = TRUE " +
                "AND expires_at > CURRENT_TIMESTAMP " +
                "AND customer_id != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, seatId);
            stmt.setInt(2, showtimeId);
            stmt.setInt(3, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Lấy danh sách ghế đang bị lock cho suất chiếu
     */
    public List<SeatLock> getLockedSeats(int showtimeId) throws SQLException {
        List<SeatLock> locks = new ArrayList<>();

        String sql = "SELECT * FROM seat_locks " +
                "WHERE showtime_id = ? AND is_active = TRUE " +
                "AND expires_at > CURRENT_TIMESTAMP " +
                "ORDER BY locked_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showtimeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    locks.add(extractSeatLockFromResultSet(rs));
                }
            }
        }

        return locks;
    }

    /**
     * Lấy locks của user hiện tại
     */
    public List<SeatLock> getUserLocks(String sessionToken, int showtimeId)
            throws SQLException {

        List<SeatLock> locks = new ArrayList<>();

        String sql = "SELECT * FROM seat_locks " +
                "WHERE session_token = ? AND showtime_id = ? AND is_active = TRUE " +
                "AND expires_at > CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionToken);
            stmt.setInt(2, showtimeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    locks.add(extractSeatLockFromResultSet(rs));
                }
            }
        }

        return locks;
    }

    /**
     * Xóa tất cả locks đã hết hạn
     */
    public int cleanupExpiredLocks(int showtimeId) throws SQLException {
        String sql = "UPDATE seat_locks SET is_active = FALSE " +
                "WHERE showtime_id = ? AND is_active = TRUE " +
                "AND expires_at <= CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showtimeId);
            int result = stmt.executeUpdate();
            conn.commit();

            return result;
        }
    }

    /**
     * Convert locks thành bookings (khi thanh toán thành công)
     */
    public boolean convertLocksToBooking(String sessionToken, int showtimeId, int bookingId)
            throws SQLException {

        String sql = "UPDATE seat_locks SET is_active = FALSE " +
                "WHERE session_token = ? AND showtime_id = ? AND is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionToken);
            stmt.setInt(2, showtimeId);

            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    // Helper method
    private SeatLock extractSeatLockFromResultSet(ResultSet rs) throws SQLException {
        SeatLock lock = new SeatLock();
        lock.setLockId(rs.getInt("lock_id"));
        lock.setSeatId(rs.getInt("seat_id"));
        lock.setShowtimeId(rs.getInt("showtime_id"));
        lock.setCustomerId(rs.getInt("customer_id"));
        lock.setSessionToken(rs.getString("session_token"));
        lock.setLockedAt(rs.getTimestamp("locked_at"));
        lock.setExpiresAt(rs.getTimestamp("expires_at"));
        lock.setActive(rs.getBoolean("is_active"));
        return lock;
    }
}