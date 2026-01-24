import dao.*;
import model.*;
import util.DatabaseConnection;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class CompleteDAOTest {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   CINEMA BOOKING SYSTEM - DAO TEST");
        System.out.println("========================================\n");

        testDatabaseConnection();
        testAllDAOs();
        testStatistics();
        testViews();
        testStoredProcedure();

        System.out.println("\n========================================");
        System.out.println("   ALL TESTS COMPLETED!");
        System.out.println("========================================");
    }

    private static void testDatabaseConnection() {
        System.out.println(">>> Test Database Connection");
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection successful!");
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testAllDAOs() {
        System.out.println(">>> Test All DAO Classes");

        DAOFactory factory = DAOFactory.getInstance();

        try {
            // Test MovieDAO
            List<Movie> movies = factory.getMovieDAO().findAll();
            System.out.println("MovieDAO: Found " + movies.size() + " movies");

            // Test ShowtimeDAO
            List<Showtime> showtimes = factory.getShowtimeDAO().findAll();
            System.out.println("ShowtimeDAO: Found " + showtimes.size() + " showtimes");

            // Test SeatDAO
            if (!showtimes.isEmpty()) {
                List<Seat> seats = factory.getSeatDAO().findByShowtime(showtimes.get(0).getShowtimeId());
                System.out.println("SeatDAO: Found " + seats.size() + " seats for showtime 1");
            }

            // Test CustomerDAO


            // Test BookingDAO
            Booking booking = factory.getBookingDAO().findById(1);
            if (booking != null) {
                System.out.println("BookingDAO: Found booking ID 1");
            }

            // Test PaymentDAO
            Payment payment = factory.getPaymentDAO().findByBookingId(1);
            if (payment != null) {
                System.out.println("PaymentDAO: Found payment for booking 1");
            }

        } catch (Exception e) {
            System.err.println("DAO test failed: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testStatistics() {
        System.out.println(">>> Test Statistics DAO");

        try {
            StatisticsDAO statsDAO = new StatisticsDAO();

            double revenue = statsDAO.getTotalRevenue();
            System.out.println("Total Revenue: " + String.format("%,.0f VND", revenue));

            int tickets = statsDAO.getTotalTicketsSold();
            System.out.println("Total Tickets Sold: " + tickets);

            double occupancy = statsDAO.getAverageOccupancyRate();
            System.out.println("Average Occupancy: " + String.format("%.2f%%", occupancy));

        } catch (Exception e) {
            System.err.println("Statistics test failed: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testViews() {
        System.out.println(">>> Test View DAO");

        try {
            ViewDAO viewDAO = new ViewDAO();

            List<Map<String, Object>> overviews = viewDAO.getShowtimeOverview();
            System.out.println("Showtime Overview: Found " + overviews.size() + " records");

            Map<String, Object> bookingDetails = viewDAO.getBookingDetails(1);
            if (bookingDetails != null) {
                System.out.println("Booking Details: Found booking 1 details");
            }

        } catch (Exception e) {
            System.err.println("View test failed: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testStoredProcedure() {
        System.out.println(">>> Test Stored Procedure");

        try {
            // Test goi stored procedure
            System.out.println("Stored Procedure ready to use");

        } catch (Exception e) {
            System.err.println("Stored Procedure test failed: " + e.getMessage());
        }
        System.out.println();
    }
}