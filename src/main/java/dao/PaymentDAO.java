package dao;

import model.Payment;
import model.PaymentMethod;
import model.PaymentStatus;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    // Luu payment moi
    public boolean save(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (booking_id, amount, payment_method, " +
                "transaction_id, payment_status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, payment.getBookingId());
            stmt.setDouble(2, payment.getAmount());
            stmt.setString(3, payment.getPaymentMethod().name());
            stmt.setString(4, payment.getTransactionId());
            stmt.setString(5, payment.getPaymentStatus().name());

            int result = stmt.executeUpdate();

            if (result > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        payment.setPaymentId(rs.getInt(1));
                    }
                }
                conn.commit();
                return true;
            }
        }

        return false;
    }

    // Lay payment theo ID
    public Payment findById(int paymentId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, paymentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPaymentFromResultSet(rs);
                }
            }
        }

        return null;
    }

    // Lay payment theo booking ID (lay payment gan nhat)
    public Payment findByBookingId(int bookingId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE booking_id = ? ORDER BY payment_time DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPaymentFromResultSet(rs);
                }
            }
        }

        return null;
    }

    // Lay tat ca payment theo booking ID
    public List<Payment> findAllByBookingId(int bookingId) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE booking_id = ? ORDER BY payment_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(extractPaymentFromResultSet(rs));
                }
            }
        }

        return payments;
    }

    // Lay payment theo transaction ID
    public Payment findByTransactionId(String transactionId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE transaction_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, transactionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPaymentFromResultSet(rs);
                }
            }
        }

        return null;
    }

    // Lay tat ca payment
    public List<Payment> findAll() throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY payment_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                payments.add(extractPaymentFromResultSet(rs));
            }
        }

        return payments;
    }

    // Lay payment theo phuong thuc thanh toan
    public List<Payment> findByPaymentMethod(PaymentMethod paymentMethod) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE payment_method = ? ORDER BY payment_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paymentMethod.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(extractPaymentFromResultSet(rs));
                }
            }
        }

        return payments;
    }

    // Lay payment theo trang thai
    public List<Payment> findByStatus(PaymentStatus status) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE payment_status = ? ORDER BY payment_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(extractPaymentFromResultSet(rs));
                }
            }
        }

        return payments;
    }

    // Lay payment theo ngay
    public List<Payment> findByDate(String date) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE DATE(payment_time) = ? ORDER BY payment_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, date);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(extractPaymentFromResultSet(rs));
                }
            }
        }

        return payments;
    }

    // Lay payment trong khoang thoi gian
    public List<Payment> findByDateRange(String startDate, String endDate) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE DATE(payment_time) BETWEEN ? AND ? " +
                "ORDER BY payment_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, startDate);
            stmt.setString(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(extractPaymentFromResultSet(rs));
                }
            }
        }

        return payments;
    }

    // Cap nhat trang thai payment
    public boolean updateStatus(int paymentId, PaymentStatus status) throws SQLException {
        String sql = "UPDATE payments SET payment_status = ? WHERE payment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, paymentId);

            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    // Cap nhat payment day du
    public boolean update(Payment payment) throws SQLException {
        String sql = "UPDATE payments SET booking_id = ?, amount = ?, payment_method = ?, " +
                "transaction_id = ?, payment_status = ? WHERE payment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, payment.getBookingId());
            stmt.setDouble(2, payment.getAmount());
            stmt.setString(3, payment.getPaymentMethod().name());
            stmt.setString(4, payment.getTransactionId());
            stmt.setString(5, payment.getPaymentStatus().name());
            stmt.setInt(6, payment.getPaymentId());

            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    // Xoa payment
    public boolean delete(int paymentId) throws SQLException {
        String sql = "DELETE FROM payments WHERE payment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, paymentId);
            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    // Kiem tra xem booking da co payment chua
    public boolean hasPayment(int bookingId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM payments WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    // Kiem tra xem booking da thanh toan thanh cong chua
    public boolean isPaymentCompleted(int bookingId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM payments " +
                "WHERE booking_id = ? AND payment_status = 'COMPLETED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    // Tinh tong tien thanh toan theo ngay
    public double getTotalAmountByDate(String date) throws SQLException {
        String sql = "SELECT SUM(amount) FROM payments " +
                "WHERE DATE(payment_time) = ? AND payment_status = 'COMPLETED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, date);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }

        return 0.0;
    }

    // Tinh tong tien theo phuong thuc thanh toan
    public double getTotalAmountByMethod(PaymentMethod method) throws SQLException {
        String sql = "SELECT SUM(amount) FROM payments " +
                "WHERE payment_method = ? AND payment_status = 'COMPLETED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, method.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }

        return 0.0;
    }

    // Dem so payment thanh cong
    public int countCompletedPayments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM payments WHERE payment_status = 'COMPLETED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    // Dem so payment that bai
    public int countFailedPayments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM payments WHERE payment_status = 'FAILED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    // Trich xuat Payment tu ResultSet
    private Payment extractPaymentFromResultSet(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setBookingId(rs.getInt("booking_id"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
        payment.setTransactionId(rs.getString("transaction_id"));
        payment.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        payment.setPaymentTime(rs.getTimestamp("payment_time"));
        return payment;
    }
}