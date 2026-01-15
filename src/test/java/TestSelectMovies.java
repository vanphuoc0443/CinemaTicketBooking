import util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestSelectMovies {
    public static void main(String[] args) {
        String sql = "SELECT movie_id, title, duration FROM movies";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("📽 Danh sách phim:");
            while (rs.next()) {
                System.out.println(
                        rs.getInt("movie_id") + " | " +
                                rs.getString("title") + " | " +
                                rs.getInt("duration") + " phút"
                );
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
