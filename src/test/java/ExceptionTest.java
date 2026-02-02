import exception.*;

public class ExceptionTest {

    public static void main(String[] args) {
        System.out.println("========== TEST EXCEPTION ==========\n");

        testBookingException();
        testSeatUnavailableException();
        testPaymentException();
        testDatabaseException();
        testCancellationException();
        testValidationException();

        System.out.println("\n========== HOAN THANH ==========");
    }

    private static void testBookingException() {
        System.out.println(">>> Test BookingException");
        try {
            throw new BookingException("Don dat ve khong hop le");
        } catch (BookingException e) {
            System.out.println("Caught: " + e.getMessage());
            System.out.println("OK\n");
        }
    }

    private static void testSeatUnavailableException() {
        System.out.println(">>> Test SeatUnavailableException");
        try {
            SeatUnavailableException ex = new SeatUnavailableException(10, "Ghe da duoc dat");
            ex.setSeatNumber("A5");
            throw ex;
        } catch (SeatUnavailableException e) {
            System.out.println("Caught: " + e.getMessage());
            System.out.println("Seat ID: " + e.getSeatId());
            System.out.println("Seat Number: " + e.getSeatNumber());
            System.out.println("OK\n");
        }
    }

    private static void testPaymentException() {
        System.out.println(">>> Test PaymentException");
        try {
            throw new PaymentException("Thanh toan that bai");
        } catch (PaymentException e) {
            System.out.println("Caught: " + e.getMessage());
            System.out.println("OK\n");
        }
    }

    private static void testDatabaseException() {
        System.out.println(">>> Test DatabaseException");
        try {
            java.sql.SQLException sqlEx = new java.sql.SQLException("Connection failed", "08001", 1045);
            throw new DatabaseException("Ket noi database that bai", sqlEx);
        } catch (DatabaseException e) {
            System.out.println("Caught: " + e.getMessage());
            System.out.println("SQL State: " + e.getSqlState());
            System.out.println("SQL Error Code: " + e.getSqlErrorCode());
            System.out.println("OK\n");
        }
    }

    private static void testCancellationException() {
        System.out.println(">>> Test CancellationException");
        try {
            throw new CancellationException(123, "Khong the huy ve");
        } catch (CancellationException e) {
            System.out.println("Caught: " + e.getMessage());
            System.out.println("Booking ID: " + e.getBookingId());
            System.out.println("OK\n");
        }
    }

    private static void testValidationException() {
        System.out.println(">>> Test ValidationException");
        try {
            ValidationException ex = new ValidationException("Validation failed");
            ex.addError("Email khong hop le");
            ex.addError("So dien thoai khong hop le");
            throw ex;
        } catch (ValidationException e) {
            System.out.println("Caught: " + e.getMessage());
            System.out.println("Has errors: " + e.hasErrors());
            System.out.println("Errors: " + e.getErrors());
            System.out.println("OK\n");
        }
    }
}