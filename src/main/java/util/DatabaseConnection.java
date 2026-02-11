package util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quan ly ket noi CSDL voi HikariCP Connection Pool
 *
 * DA SUA:
 * - Loai bo hardcode mat khau
 * - Ho tro bien moi truong
 * - Ho tro file cau hinh
 * - Xu ly loi dung chuan
 * - Ho tro logging
 */
public class DatabaseConnection {

    private static final HikariDataSource dataSource;
    private static final Properties dbConfig = new Properties();

    static {
        try {
            // Tai cau hinh
            loadConfiguration();

            // Thiet lap HikariCP
            HikariConfig config = new HikariConfig();

            // URL CSDL
            String dbUrl = getConfigValue("db.url",
                    "jdbc:mysql://localhost:3306/cinema_booking");
            config.setJdbcUrl(dbUrl +
                    "?useSSL=false" +
                    "&serverTimezone=UTC" +
                    "&allowPublicKeyRetrieval=true");

            // Thong tin dang nhap
            config.setUsername(getConfigValue("db.username", "root"));
            config.setPassword(getConfigValue("db.password", ""));

            // Driver
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            // Cau hinh Pool
            config.setMaximumPoolSize(
                    Integer.parseInt(getConfigValue("db.pool.max.size", "10")));
            config.setMinimumIdle(
                    Integer.parseInt(getConfigValue("db.pool.min.idle", "2")));
            config.setConnectionTimeout(
                    Long.parseLong(getConfigValue("db.pool.connection.timeout", "30000")));
            config.setIdleTimeout(
                    Long.parseLong(getConfigValue("db.pool.idle.timeout", "600000")));
            config.setMaxLifetime(
                    Long.parseLong(getConfigValue("db.pool.max.lifetime", "1800000")));

            // Tu dong commit
            config.setAutoCommit(false);

            // Ten Pool
            config.setPoolName("CinemaBooking-HikariCP");

            // Phat hien ro ri (ho tro debug)
            config.setLeakDetectionThreshold(
                    Long.parseLong(getConfigValue("db.pool.leak.detection", "60000")));

            dataSource = new HikariDataSource(config);

            System.out.println("Khoi tao connection pool thanh cong");

        } catch (Exception e) {
            System.err.println("Failed to initialize database connection pool: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Tai cau hinh tu nhieu nguon (thu tu uu tien):
     * 1. Bien moi truong (cao nhat)
     * 2. File config.properties
     * 3. Gia tri mac dinh
     */
    private static void loadConfiguration() {
        // Thu tai tu config.properties
        try {
            // Thu tu classpath truoc
            InputStream input = DatabaseConnection.class
                    .getClassLoader()
                    .getResourceAsStream("config.properties");

            if (input != null) {
                dbConfig.load(input);
                System.out.println("Da tai config tu classpath:config.properties");
                input.close();
            } else {
                // Thu tu file system
                try (FileInputStream fileInput = new FileInputStream("config.properties")) {
                    dbConfig.load(fileInput);
                    System.out.println("Da tai config tu file:config.properties");
                } catch (IOException e) {
                    System.out.println("Khong tim thay config.properties, dung bien moi truong va mac dinh");
                }
            }
        } catch (IOException e) {
            System.out.println("Khong the tai config.properties: " + e.getMessage());
        }
    }

    /**
     * Lay gia tri cau hinh tu nhieu nguon
     * Uu tien: Bien moi truong > File Properties > Mac dinh
     */
    private static String getConfigValue(String key, String defaultValue) {
        // 1. Thu bien moi truong (doi dau cham thanh gach duoi va viet hoa)
        String envKey = key.replace(".", "_").toUpperCase();
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        // 2. Thu file properties
        String propValue = dbConfig.getProperty(key);
        if (propValue != null && !propValue.isEmpty()) {
            return propValue;
        }

        // 3. Dung gia tri mac dinh
        return defaultValue;
    }

    /**
     * Constructor private de ngan tao instance
     */
    private DatabaseConnection() {
    }

    /**
     * Lay ket noi CSDL tu pool
     *
     * @return Doi tuong Connection
     * @throws SQLException neu khong lay duoc ket noi
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Lay thong ke pool de giam sat
     */
    public static String getPoolStats() {
        return String.format(
                "Pool Stats: Active=%d, Idle=%d, Waiting=%d, Total=%d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection(),
                dataSource.getHikariPoolMXBean().getTotalConnections());
    }

    /**
     * Kiem tra suc khoe CSDL
     */
    public static boolean isHealthy() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Dong connection pool
     * Goi khi ung dung tat
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Da dong database connection pool");
        }
    }
}