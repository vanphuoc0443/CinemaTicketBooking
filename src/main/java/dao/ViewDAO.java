package dao;

import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewDAO {

    // Lay thong tin tu booking_details view
    public Map<String, Object> getBookingDetails(int bookingId) throws SQLException {
        String sql = "SELECT * FROM booking_details WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("booking_id", rs.getInt("booking_id"));
                    details.put("customer_name", rs.getString("customer_name"));
                    details.put("customer_email", rs.getString("customer_email"));
                    details.put("movie_title", rs.getString("movie_title"));
                    details.put("show_date", rs.getString("show_date"));
                    details.put("show_time", rs.getString("show_time"));
                    details.put("room_number", rs.getInt("room_number"));
                    details.put("seats", rs.getString("seats"));
                    details.put("total_amount", rs.getDouble("total_amount"));
                    details.put("status", rs.getString("status"));
                    details.put("booking_time", rs.getTimestamp("booking_time"));
                    return details;
                }
            }
        }

        return null;
    }

    // Lay tong quan suat chieu tu showtime_overview view
    public List<Map<String, Object>> getShowtimeOverview() throws SQLException {
        List<Map<String, Object>> overviews = new ArrayList<>();
        String sql = "SELECT * FROM showtime_overview ORDER BY show_date, show_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> overview = new HashMap<>();
                overview.put("showtime_id", rs.getInt("showtime_id"));
                overview.put("movie_title", rs.getString("movie_title"));
                overview.put("duration", rs.getInt("duration"));
                overview.put("show_date", rs.getString("show_date"));
                overview.put("show_time", rs.getString("show_time"));
                overview.put("room_number", rs.getInt("room_number"));
                overview.put("total_seats", rs.getInt("total_seats"));
                overview.put("available_seats", rs.getInt("available_seats"));
                overview.put("booked_seats", rs.getInt("booked_seats"));
                overview.put("occupancy_rate", rs.getDouble("occupancy_rate"));
                overviews.add(overview);
            }
        }

        return overviews;
    }

    // Lay tong quan suat chieu theo phim
    public List<Map<String, Object>> getShowtimeOverviewByMovie(int movieId) throws SQLException {
        List<Map<String, Object>> overviews = new ArrayList<>();
        String sql = "SELECT so.* FROM showtime_overview so " +
                "JOIN showtimes s ON so.showtime_id = s.showtime_id " +
                "WHERE s.movie_id = ? ORDER BY so.show_date, so.show_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> overview = new HashMap<>();
                    overview.put("showtime_id", rs.getInt("showtime_id"));
                    overview.put("movie_title", rs.getString("movie_title"));
                    overview.put("duration", rs.getInt("duration"));
                    overview.put("show_date", rs.getString("show_date"));
                    overview.put("show_time", rs.getString("show_time"));
                    overview.put("room_number", rs.getInt("room_number"));
                    overview.put("total_seats", rs.getInt("total_seats"));
                    overview.put("available_seats", rs.getInt("available_seats"));
                    overview.put("booked_seats", rs.getInt("booked_seats"));
                    overview.put("occupancy_rate", rs.getDouble("occupancy_rate"));
                    overviews.add(overview);
                }
            }
        }

        return overviews;
    }
}