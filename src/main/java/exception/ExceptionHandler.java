package exception;

import javax.swing.JOptionPane;

public class ExceptionHandler {

    public static void show(Exception e) {
        String title = "Loi";
        String message = e.getMessage();
        int type = JOptionPane.ERROR_MESSAGE;

        if (e instanceof SeatUnavailableException) {
            title = "Ghe Khong Con Trong";
            SeatUnavailableException ex = (SeatUnavailableException) e;
            if (ex.getSeatNumber() != null && !ex.getSeatNumber().isEmpty()) {
                message = "Ghe " + ex.getSeatNumber() + " da duoc dat. Vui long chon ghe khac.";
            }
        } else if (e instanceof BookingException) {
            title = "Loi Dat Ve";
        } else if (e instanceof PaymentException) {
            title = "Loi Thanh Toan";
        } else if (e instanceof CancellationException) {
            title = "Loi Huy Ve";
        } else if (e instanceof DatabaseException) {
            title = "Loi He Thong";
            DatabaseException ex = (DatabaseException) e;
            if (ex.getSqlException() != null) {
                message = message + "\nMa loi SQL: " + ex.getSqlErrorCode();
            }
        } else if (e instanceof ValidationException) {
            title = "Loi Nhap Lieu";
            type = JOptionPane.WARNING_MESSAGE;
        }

        JOptionPane.showMessageDialog(null, message, title, type);
    }

    public static void log(Exception e) {
        System.err.println("========== EXCEPTION ==========");
        System.err.println("Time: " + new java.util.Date());
        System.err.println("Type: " + e.getClass().getSimpleName());
        System.err.println("Message: " + e.getMessage());
        System.err.println("Stack Trace:");
        e.printStackTrace();
        System.err.println("===============================\n");
    }

    public static void showAndLog(Exception e) {
        log(e);
        show(e);
    }
}