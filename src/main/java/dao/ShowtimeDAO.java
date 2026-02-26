package dao;

import model.Showtime;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShowtimeDAO {

    // Lay tat ca suat chieu
    public List<Showtime> findAll() throws SQLException {
        List<Showtime> showtimes = new ArrayList<>();
        String sql = "SELECT s.*, m.title as movie_title FROM showtimes s " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "ORDER BY s.show_date, s.show_time";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                showtimes.add(extractShowtimeFromResultSet(rs));
            }
        }

        return showtimes;
    }

    // Lay suat chieu theo ID
    public Showtime findById(int showtimeId) throws SQLException {
        String sql = "SELECT s.*, m.title as movie_title FROM showtimes s " +
                "JOIN movies m ON s.movie_id = m.movie_id WHERE s.showtime_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showtimeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractShowtimeFromResultSet(rs);
                }
            }
        }

        return null;
    }

    // Lay suat chieu theo phim
    public List<Showtime> findByMovieId(int movieId) throws SQLException {
        List<Showtime> showtimes = new ArrayList<>();
        String sql = "SELECT s.*, m.title as movie_title FROM showtimes s " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "WHERE s.movie_id = ? ORDER BY s.show_date, s.show_time";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    showtimes.add(extractShowtimeFromResultSet(rs));
                }
            }
        }

        return showtimes;
    }

    // Lay suat chieu theo ngay
    public List<Showtime> findByDate(String date) throws SQLException {
        List<Showtime> showtimes = new ArrayList<>();
        String sql = "SELECT s.*, m.title as movie_title FROM showtimes s " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "WHERE s.show_date = ? ORDER BY s.show_time";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, date);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    showtimes.add(extractShowtimeFromResultSet(rs));
                }
            }
        }

        return showtimes;
    }

    // Kiem tra xung dot phong chieu (exact time match)
    public boolean checkRoomConflict(int roomNumber, String showDate, String showTime) throws SQLException {
        String sql = "SELECT COUNT(*) FROM showtimes " +
                "WHERE room_number = ? AND show_date = ? AND show_time = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomNumber);
            stmt.setString(2, showDate);
            stmt.setString(3, showTime);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Kiểm tra xung đột thời gian dựa trên duration của phim.
     * Tính endTime = startTime + movieDuration + 15 phút (dọn dẹp).
     * Kiểm tra overlap với tất cả suất chiếu cùng phòng cùng ngày.
     *
     * @param roomNumber  Phòng chiếu
     * @param showDate    Ngày chiếu (yyyy-MM-dd)
     * @param startTime   Giờ bắt đầu (HH:mm)
     * @param durationMin Thời lượng phim (phút)
     * @param excludeId   ID suất chiếu cần loại trừ (khi update), -1 nếu không cần
     * @return true nếu có xung đột
     */
    public boolean checkTimeOverlap(int roomNumber, String showDate, String startTime,
            int durationMin, int excludeId) throws SQLException {
        // Calculate new showtime range: [startTime, startTime + duration + 15min
        // buffer]
        int bufferMin = 15;
        int totalMin = durationMin + bufferMin;

        // Query all showtimes in the same room on the same date
        String sql = "SELECT s.show_time, m.duration FROM showtimes s " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "WHERE s.room_number = ? AND s.show_date = ?";
        if (excludeId > 0) {
            sql += " AND s.showtime_id != ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomNumber);
            stmt.setString(2, showDate);
            if (excludeId > 0) {
                stmt.setInt(3, excludeId);
            }

            // Parse new showtime start/end
            int newStartMin = parseTimeToMinutes(startTime);
            int newEndMin = newStartMin + totalMin;

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String existingTime = rs.getString("show_time");
                    int existingDuration = rs.getInt("duration");

                    int existStart = parseTimeToMinutes(existingTime);
                    int existEnd = existStart + existingDuration + bufferMin;

                    // Check overlap: two ranges [A,B] and [C,D] overlap if A < D && C < B
                    if (newStartMin < existEnd && existStart < newEndMin) {
                        return true; // CONFLICT!
                    }
                }
            }
        }
        return false;
    }

    // Đếm suất chiếu theo phim
    public int countByMovieId(int movieId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM showtimes WHERE movie_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // Lấy suất chiếu theo phòng và ngày
    public List<Showtime> findByRoomAndDate(int roomNumber, String date) throws SQLException {
        List<Showtime> showtimes = new ArrayList<>();
        String sql = "SELECT s.*, m.title as movie_title FROM showtimes s " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "WHERE s.room_number = ? AND s.show_date = ? ORDER BY s.show_time";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomNumber);
            stmt.setString(2, date);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    showtimes.add(extractShowtimeFromResultSet(rs));
                }
            }
        }
        return showtimes;
    }

    // Lấy suất chiếu theo phim và ngày (cho user app)
    public List<Showtime> findByMovieAndDate(int movieId, String date) throws SQLException {
        List<Showtime> showtimes = new ArrayList<>();
        String sql = "SELECT s.*, m.title as movie_title FROM showtimes s " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "WHERE s.movie_id = ? AND s.show_date = ? ORDER BY s.show_time";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            stmt.setString(2, date);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    showtimes.add(extractShowtimeFromResultSet(rs));
                }
            }
        }
        return showtimes;
    }

    /**
     * Parse time string "HH:mm:ss" or "HH:mm" to total minutes since midnight.
     */
    private int parseTimeToMinutes(String time) {
        if (time == null)
            return 0;
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return hours * 60 + minutes;
    }

    // Them suat chieu moi
    public boolean save(Showtime showtime) throws SQLException {
        String sql = "INSERT INTO showtimes (movie_id, show_date, show_time, room_number, " +
                "total_seats, available_seats) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, showtime.getMovieId());
            stmt.setString(2, showtime.getShowDate());
            stmt.setString(3, String.valueOf(showtime.getShowTime()));
            stmt.setInt(4, showtime.getRoomNumber());
            stmt.setInt(5, showtime.getTotalSeats());
            stmt.setInt(6, showtime.getAvailableSeats());

            int result = stmt.executeUpdate();

            if (result > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        showtime.setShowtimeId(rs.getInt(1));
                    }
                }
                conn.commit();
                return true;
            }
        }

        return false;
    }

    // Cap nhat suat chieu
    public boolean update(Showtime showtime) throws SQLException {
        String sql = "UPDATE showtimes SET movie_id = ?, show_date = ?, show_time = ?, " +
                "room_number = ? WHERE showtime_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showtime.getMovieId());
            stmt.setString(2, showtime.getShowDate());
            stmt.setString(3, String.valueOf(showtime.getShowTime()));
            stmt.setInt(4, showtime.getRoomNumber());
            stmt.setInt(5, showtime.getShowtimeId());

            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    // Xoa suat chieu
    public boolean delete(int showtimeId) throws SQLException {
        String sql = "DELETE FROM showtimes WHERE showtime_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showtimeId);
            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    // Trich xuat Showtime tu ResultSet
    private Showtime extractShowtimeFromResultSet(ResultSet rs) throws SQLException {
        Showtime showtime = new Showtime();
        showtime.setShowtimeId(rs.getInt("showtime_id"));
        showtime.setMovieId(rs.getInt("movie_id"));
        showtime.setShowDate(rs.getString("show_date"));
        showtime.setShowTime(rs.getString("show_time"));
        showtime.setRoomNumber(rs.getInt("room_number"));
        showtime.setTotalSeats(rs.getInt("total_seats"));
        showtime.setAvailableSeats(rs.getInt("available_seats"));
        showtime.setCreatedAt(rs.getTimestamp("created_at"));
        showtime.setUpdatedAt(rs.getTimestamp("updated_at"));
        showtime.setMovieTitle(rs.getString("movie_title"));
        return showtime;
    }
}
