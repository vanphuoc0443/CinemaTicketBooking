import util.DatabaseConnection;

import java.sql.Connection;

public class TestDatabase {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Connect server thành công");
            System.out.println("AutoCommit: " + conn.getAutoCommit());
        } catch (Exception e) {
            System.out.println("Connect server thất bại");
            e.printStackTrace();
        }
    }
}
