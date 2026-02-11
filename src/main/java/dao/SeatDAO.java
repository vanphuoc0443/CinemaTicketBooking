package dao;

import model.Seat;
import model.SeatType;
import model.SeatStatus;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatDAO {

    // Lay ghe theo ID
    public Seat findById(int seatId) throws SQLException {
        String sql = "SELECT * FROM seats WHERE seat_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, seatId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractSeatFromResultSet(rs);
                }
            }
        }

        return null;
    }

    // Lay nhieu ghe theo IDs
    public List<Seat> findByIds(List<Integer> seatIds) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        if (seatIds == null || seatIds.isEmpty()) {
            return seats;
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM seats WHERE seat_id IN (");
        for (int i = 0; i < seatIds.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < seatIds.size(); i++) {
                stmt.setInt(i + 1, seatIds.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(extractSeatFromResultSet(rs));
                }
            }
        }
        return seats;
    }

    // Khoa ghe de cap nhat (Pessimistic Locking)
    public Seat lockSeatForUpdate(int seatId) throws SQLException {
        String sql = "SELECT * FROM seats WHERE seat_id = ? FOR UPDATE";

        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setInt(1, seatId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return extractSeatFromResultSet(rs);
        }

        return null;
    }

    // Lay tat ca ghe theo suat chieu
    public List<Seat> findByShowtime(int showtimeId) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE showtime_id = ? ORDER BY seat_number";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showtimeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(extractSeatFromResultSet(rs));
                }
            }
        }

        return seats;
    }

    // Lay ghe con trong
    public List<Seat> findAvailableSeats(int showtimeId) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE showtime_id = ? AND status = 'AVAILABLE' " +
                "ORDER BY seat_number";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showtimeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(extractSeatFromResultSet(rs));
                }
            }
        }

        return seats;
    }

    // Cap nhat trang thai ghe
    public boolean updateSeatStatus(int seatId, SeatStatus status) throws SQLException {
        String sql = "UPDATE seats SET status = ?, version = version + 1 WHERE seat_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, seatId);

            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    // Giai phong ghe (chuyen ve AVAILABLE)
    public boolean releaseSeat(int seatId) throws SQLException {
        return updateSeatStatus(seatId, SeatStatus.AVAILABLE);
    }

    // Luu nhieu ghe cung luc (Batch Insert)
    public boolean saveAll(List<Seat> seats) throws SQLException {
        String sql = "INSERT INTO seats (showtime_id, seat_number, seat_type, status, price, version) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Seat seat : seats) {
                stmt.setInt(1, seat.getShowtimeId());
                stmt.setString(2, seat.getSeatNumber());
                stmt.setString(3, seat.getSeatType().name());
                stmt.setString(4, seat.getStatus().name());
                stmt.setDouble(5, seat.getPrice());
                stmt.setInt(6, seat.getVersion());
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

    // Trich xuat Seat tu ResultSet
    private Seat extractSeatFromResultSet(ResultSet rs) throws SQLException {
        Seat seat = new Seat();
        seat.setSeatId(rs.getInt("seat_id"));
        seat.setShowtimeId(rs.getInt("showtime_id"));
        seat.setSeatNumber(rs.getString("seat_number"));
        seat.setSeatType(SeatType.valueOf(rs.getString("seat_type")));
        seat.setStatus(SeatStatus.valueOf(rs.getString("status")));
        seat.setPrice(rs.getDouble("price"));
        seat.setVersion(rs.getInt("version"));
        seat.setCreatedAt(rs.getTimestamp("created_at"));
        seat.setUpdatedAt(rs.getTimestamp("updated_at"));
        return seat;
    }
}