import util.DatabaseConnection;
import java.sql.*;

public class TestTransactionRollback {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            try {
                PreparedStatement ps1 = conn.prepareStatement(
                        "INSERT INTO movies(title, duration) VALUES (?, ?)");
                ps1.setString(1, "ROLLBACK_TEST");
                ps1.setInt(2, 100);
                ps1.executeUpdate();

                PreparedStatement ps2 = conn.prepareStatement(
                        "INSERT INTO movies(title, duration) VALUES (?, ?)");
                ps2.setString(1, null); // gây lỗi NOT NULL
                ps2.setInt(2, 90);
                ps2.executeUpdate();

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                System.out.println("ERROR OCCURRED → ROLLBACK SUCCESS");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
