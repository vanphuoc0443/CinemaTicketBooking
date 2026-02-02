package dao;

import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingSeatDAO {

    // Lay danh sach seat ID theo booking
    public List<Integer> getSeatIdsByBookingId(int bookingId) throws SQLException {
        List<Integer> seatIds = new ArrayList<>();
        String sql = "SELECT seat_id FROM booking_seats WHERE booking_id = ? ORDER BY seat_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seatIds.add(rs.getInt("seat_id"));
                }
            }
        }

        return seatIds;
    }

    // Lay danh sach booking ID theo seat
    public List<Integer> getBookingIdsBySeatId(int seatId) throws SQLException {
        List<Integer> bookingIds = new ArrayList<>();
        String sql = "SELECT booking_id FROM booking_seats WHERE seat_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, seatId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookingIds.add(rs.getInt("booking_id"));
                }
            }
        }

        return bookingIds;
    }

    // Them lien ket booking-seat
    public boolean addBookingSeat(int bookingId, int seatId) throws SQLException {
        String sql = "INSERT INTO booking_seats (booking_id, seat_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            stmt.setInt(2, seatId);

            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    // Them nhieu lien ket (batch)
    public boolean addBookingSeats(int bookingId, List<Integer> seatIds) throws SQLException {
        String sql = "INSERT INTO booking_seats (booking_id, seat_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int seatId : seatIds) {
                stmt.setInt(1, bookingId);
                stmt.setInt(2, seatId);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            conn.commit();

            for (int result : results) {
                if (result == Statement.EXECUTE_FAILED) {
                    return false;
                }
            }

            return true;
        }
    }

    // Xoa lien ket theo booking
    public boolean deleteByBookingId(int bookingId) throws SQLException {
        String sql = "DELETE FROM booking_seats WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    // Dem so ghe trong booking
    public int countSeatsByBookingId(int bookingId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM booking_seats WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }
}
