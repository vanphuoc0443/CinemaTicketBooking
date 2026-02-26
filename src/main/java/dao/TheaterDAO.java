package dao;

import model.Theater;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TheaterDAO {

    private static final int MAX_THEATERS = 10;

    // Lấy tất cả phòng chiếu
    public List<Theater> findAll() throws SQLException {
        List<Theater> theaters = new ArrayList<>();
        String sql = "SELECT * FROM theaters WHERE is_active = TRUE ORDER BY theater_id";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                theaters.add(extractTheater(rs));
            }
        }
        return theaters;
    }

    // Lấy phòng chiếu theo ID
    public Theater findById(int theaterId) throws SQLException {
        String sql = "SELECT * FROM theaters WHERE theater_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractTheater(rs);
                }
            }
        }
        return null;
    }

    // Đếm số phòng chiếu đang hoạt động
    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM theaters WHERE is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // Thêm phòng chiếu mới (max 10)
    public boolean save(Theater theater) throws SQLException {
        if (countActive() >= MAX_THEATERS) {
            throw new SQLException("Đã đạt tối đa " + MAX_THEATERS + " phòng chiếu!");
        }

        String sql = "INSERT INTO theaters (name, total_seats, is_active) VALUES (?, ?, TRUE)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, theater.getName());
            stmt.setInt(2, theater.getTotalSeats());

            int result = stmt.executeUpdate();
            if (result > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        theater.setTheaterId(rs.getInt(1));
                    }
                }
                conn.commit();
                return true;
            }
        }
        return false;
    }

    // Xóa phòng chiếu (soft delete)
    public boolean delete(int theaterId) throws SQLException {
        String sql = "UPDATE theaters SET is_active = FALSE WHERE theater_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            int result = stmt.executeUpdate();
            conn.commit();
            return result > 0;
        }
    }

    // Kiểm tra phòng chiếu có suất chiếu tương lai không
    public boolean hasActiveShowtimes(int theaterId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM showtimes WHERE room_number = ? AND show_date >= CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Lấy số phòng tiếp theo (auto-numbered)
    public int getNextTheaterNumber() throws SQLException {
        String sql = "SELECT COALESCE(MAX(theater_id), 0) + 1 FROM theaters";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 1;
    }

    private Theater extractTheater(ResultSet rs) throws SQLException {
        Theater t = new Theater();
        t.setTheaterId(rs.getInt("theater_id"));
        t.setName(rs.getString("name"));
        t.setTotalSeats(rs.getInt("total_seats"));
        t.setActive(rs.getBoolean("is_active"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        return t;
    }
}
