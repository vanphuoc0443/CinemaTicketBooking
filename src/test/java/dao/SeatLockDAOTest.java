package dao;

import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style test for SeatLockDAO using H2 in-memory database.
 * Avoids mocking java.sql interfaces which is incompatible with Java 23+.
 */
public class SeatLockDAOTest {

    private static Connection conn;
    private SeatLockDAO seatLockDAO;

    @BeforeAll
    public static void setupDatabase() throws SQLException {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");

        try (Statement stmt = conn.createStatement()) {
            // Create seat_locks table matching the expected schema
            stmt.execute("CREATE TABLE IF NOT EXISTS seat_locks (" +
                    "lock_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "seat_id INT NOT NULL, " +
                    "showtime_id INT NOT NULL, " +
                    "customer_id INT NOT NULL, " +
                    "session_token VARCHAR(255) NOT NULL, " +
                    "locked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "expires_at TIMESTAMP NOT NULL, " +
                    "is_active BOOLEAN DEFAULT TRUE, " +
                    "booking_id INT DEFAULT NULL)");
        }
    }

    @BeforeEach
    public void setUp() throws SQLException {
        seatLockDAO = new SeatLockDAO();
        // Clean up before each test
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM seat_locks");
        }
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        if (conn != null) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS seat_locks");
            }
            conn.close();
        }
    }

    @Test
    @DisplayName("lockSeats should return false for empty seat list")
    public void testLockSeats_EmptyList() throws SQLException {
        List<Integer> emptySeatIds = Arrays.asList();
        boolean result = seatLockDAO.lockSeats(emptySeatIds, 1, 1, "token");
        assertFalse(result, "Should return false for empty seat list");
    }

    @Test
    @DisplayName("lockSeats should return false for null seat list")
    public void testLockSeats_NullList() throws SQLException {
        boolean result = seatLockDAO.lockSeats(null, 1, 1, "token");
        assertFalse(result, "Should return false for null seat list");
    }

    @Test
    @DisplayName("Verify SeatLockDAO class exists and has lockSeats method")
    public void testSeatLockDAO_HasLockSeatsMethod() {
        assertNotNull(seatLockDAO);

        // Verify the method signature exists via reflection
        try {
            var method = SeatLockDAO.class.getMethod("lockSeats",
                    List.class, int.class, int.class, String.class);
            assertNotNull(method);
            assertEquals(boolean.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            fail("SeatLockDAO should have lockSeats(List, int, int, String) method");
        }
    }

    @Test
    @DisplayName("Verify SeatLockDAO has all required public methods")
    public void testSeatLockDAO_HasAllRequiredMethods() {
        Class<SeatLockDAO> clazz = SeatLockDAO.class;

        assertDoesNotThrow(() -> clazz.getMethod("lockSeats", List.class, int.class, int.class, String.class),
                "Should have lockSeats method");
        assertDoesNotThrow(() -> clazz.getMethod("isSeatLocked", int.class, int.class, int.class),
                "Should have isSeatLocked method");
        assertDoesNotThrow(() -> clazz.getMethod("unlockSeat", int.class, int.class, String.class),
                "Should have unlockSeat method");
        assertDoesNotThrow(() -> clazz.getMethod("getUserLocks", String.class, int.class),
                "Should have getUserLocks method");
        assertDoesNotThrow(() -> clazz.getMethod("getLockedSeats", int.class),
                "Should have getLockedSeats method");
    }
}
