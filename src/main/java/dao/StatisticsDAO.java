package dao;

import util.DatabaseConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class StatisticsDAO {

    // Tong doanh thu
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM bookings WHERE status = 'CONFIRMED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        }

        return 0.0;
    }

    // Doanh thu theo ngay
    public double getRevenueByDate(String date) throws SQLException {
        String sql = "SELECT SUM(b.total_amount) FROM bookings b " +
                "JOIN showtimes s ON b.showtime_id = s.showtime_id " +
                "WHERE s.show_date = ? AND b.status = 'CONFIRMED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, date);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }

        return 0.0;
    }

    // Doanh thu theo phim
    public Map<String, Double> getRevenueByMovie() throws SQLException {
        Map<String, Double> revenueMap = new HashMap<>();
        String sql = "SELECT m.title, SUM(b.total_amount) as revenue " +
                "FROM bookings b " +
                "JOIN showtimes s ON b.showtime_id = s.showtime_id " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "WHERE b.status = 'CONFIRMED' " +
                "GROUP BY m.movie_id, m.title " +
                "ORDER BY revenue DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                revenueMap.put(rs.getString("title"), rs.getDouble("revenue"));
            }
        }

        return revenueMap;
    }

    // Tong so ve da ban
    public int getTotalTicketsSold() throws SQLException {
        String sql = "SELECT COUNT(*) FROM booking_seats bs " +
                "JOIN bookings b ON bs.booking_id = b.booking_id " +
                "WHERE b.status = 'CONFIRMED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    // Ty le lap day trung binh
    public double getAverageOccupancyRate() throws SQLException {
        String sql = "SELECT AVG((total_seats - available_seats) * 100.0 / total_seats) " +
                "FROM showtimes WHERE show_date >= CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        }

        return 0.0;
    }

    // Phim pho bien nhat (theo so ve)
    public Map<String, Integer> getPopularMovies(int limit) throws SQLException {
        Map<String, Integer> popularMovies = new HashMap<>();
        String sql = "SELECT m.title, COUNT(bs.seat_id) as ticket_count " +
                "FROM booking_seats bs " +
                "JOIN bookings b ON bs.booking_id = b.booking_id " +
                "JOIN showtimes s ON b.showtime_id = s.showtime_id " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "WHERE b.status = 'CONFIRMED' " +
                "GROUP BY m.movie_id, m.title " +
                "ORDER BY ticket_count DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    popularMovies.put(rs.getString("title"), rs.getInt("ticket_count"));
                }
            }
        }

        return popularMovies;
    }

    // Khach hang tich cuc nhat
    public Map<String, Integer> getTopCustomers(int limit) throws SQLException {
        Map<String, Integer> topCustomers = new HashMap<>();
        String sql = "SELECT c.name, COUNT(b.booking_id) as booking_count " +
                "FROM customers c " +
                "JOIN bookings b ON c.customer_id = b.customer_id " +
                "WHERE b.status = 'CONFIRMED' " +
                "GROUP BY c.customer_id, c.name " +
                "ORDER BY booking_count DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topCustomers.put(rs.getString("name"), rs.getInt("booking_count"));
                }
            }
        }

        return topCustomers;
    }
}