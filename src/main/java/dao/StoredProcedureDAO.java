package dao;

import java.sql.*;

// Goi stored procedure tao ghe tu dong
public class StoredProcedureDAO {

    // Tao 80 ghe cho suat chieu
    public static boolean createSeatsForShowtime(Connection conn, int showtimeId) throws SQLException {
        String sql = "{CALL create_seats_for_showtime(?)}";

        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, showtimeId);
            stmt.execute();
            return true;
        }
    }

    // Tao 80 ghe cho suat chieu (tu dong commit)
    public static boolean createSeatsForShowtime(int showtimeId) throws SQLException {
        String sql = "{CALL create_seats_for_showtime(?)}";

        try (Connection conn = util.DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, showtimeId);
            stmt.execute();
            conn.commit();

            return true;
        }
    }
}