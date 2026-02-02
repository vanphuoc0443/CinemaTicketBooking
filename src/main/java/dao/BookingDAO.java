package dao;

import model.Booking;
import model.BookingStatus;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    // Luu booking moi va tra ve ID
    public int save(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (customer_id, showtime_id, total_amount, status) " +
                "VALUES (?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        stmt.setInt(1, booking.getCustomerId());
        stmt.setInt(2, booking.getShowtimeId());
        stmt.setDouble(3, booking.getTotalAmount());
        stmt.setString(4, booking.getStatus().name());

        int result = stmt.executeUpdate();

        if (result > 0) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int bookingId = rs.getInt(1);

                // Luu booking_seats
                String seatSql = "INSERT INTO booking_seats (booking_id, seat_id) VALUES (?, ?)";
                PreparedStatement seatStmt = conn.prepareStatement(seatSql);

                for (int seatId : booking.getSeatIds()) {
                    seatStmt.setInt(1, bookingId);
                    seatStmt.setInt(2, seatId);
                    seatStmt.addBatch();
                }

                seatStmt.executeBatch();
                conn.commit();

                return bookingId;
            }
        }

        return -1;
    }

    // Lay booking theo ID
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
                    PreparedStatement seatStmt = conn.prepareStatement(seatSql);
                    seatStmt.setInt(1, bookingId);
                    ResultSet seatRs = seatStmt.executeQuery();

                    while (seatRs.next()) {
                        booking.addSeatId(seatRs.getInt("seat_id"));
                    }

                    return booking;
                }
            }
        }

        return null;
    }

    // Lay booking theo khach hang
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
        }

        return bookings;
    }

    // Lay booking theo suat chieu
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
        }

        return bookings;
    }

    // Cap nhat trang thai booking
    public boolean updateStatus(int bookingId, BookingStatus status) throws SQLException {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, bookingId);

            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    // Xoa booking
    public boolean delete(int bookingId) throws SQLException {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    // Trich xuat Booking tu ResultSet
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
