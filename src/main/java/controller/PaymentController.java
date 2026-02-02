package controller;

import dao.BookingDAO;
import dao.PaymentDAO;
import dao.TransactionManager;
import model.Booking;
import model.Payment;
import model.PaymentMethod;
import model.PaymentStatus;
import model.BookingStatus;
import exception.PaymentException;
import exception.DatabaseException;
import java.sql.SQLException;

public class PaymentController {
    private PaymentDAO paymentDAO;
    private BookingDAO bookingDAO;
    private TransactionManager transactionManager;

    public PaymentController(PaymentDAO paymentDAO, BookingDAO bookingDAO,
                             TransactionManager transactionManager) {
        this.paymentDAO = paymentDAO;
        this.bookingDAO = bookingDAO;
        this.transactionManager = transactionManager;
    }

    // Xu ly thanh toan
    public Payment processPayment(int bookingId, PaymentMethod paymentMethod, double amount)
            throws PaymentException, DatabaseException {

        try {
            return (Payment) transactionManager.executeInTransaction(() -> {
                Booking booking = bookingDAO.findById(bookingId);

                if (booking == null) {
                    throw new PaymentException("Khong tim thay don dat ve");
                }

                if (booking.getStatus() != BookingStatus.PENDING) {
                    throw new PaymentException("Don dat ve khong o trang thai cho thanh toan");
                }

                if (Math.abs(amount - booking.getTotalAmount()) > 0.01) {
                    throw new PaymentException("So tien khong khop voi don dat ve");
                }

                // Tao payment
                Payment payment = new Payment();
                payment.setBookingId(bookingId);
                payment.setAmount(amount);
                payment.setPaymentMethod(paymentMethod);
                payment.setTransactionId(generateTransactionId());
                payment.setPaymentStatus(PaymentStatus.PENDING);

                // Luu payment
                boolean saved = paymentDAO.save(payment);

                if (!saved) {
                    throw new PaymentException("Khong the luu thong tin thanh toan");
                }

                // Gia lap xu ly thanh toan
                boolean paymentSuccess = simulatePaymentGateway(payment);

                if (paymentSuccess) {
                    payment.setPaymentStatus(PaymentStatus.COMPLETED);
                    paymentDAO.updateStatus(payment.getPaymentId(), PaymentStatus.COMPLETED);

                    // Cap nhat trang thai booking
                    bookingDAO.updateStatus(bookingId, BookingStatus.CONFIRMED);
                } else {
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                    paymentDAO.updateStatus(payment.getPaymentId(), PaymentStatus.FAILED);
                    throw new PaymentException("Thanh toan that bai");
                }

                return payment;
            });

        } catch (SQLException e) {
            throw new DatabaseException("Loi khi xu ly thanh toan", e);
        }
    }

    // Hoan tien
    public boolean refundPayment(int bookingId) throws PaymentException, DatabaseException {
        try {
            return (boolean) transactionManager.executeInTransaction(() -> {
                Payment payment = paymentDAO.findByBookingId(bookingId);

                if (payment == null) {
                    throw new PaymentException("Khong tim thay thong tin thanh toan");
                }

                if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
                    throw new PaymentException("Chi hoan tien cho thanh toan thanh cong");
                }

                // Tao payment moi cho refund
                Payment refund = new Payment();
                refund.setBookingId(bookingId);
                refund.setAmount(payment.getAmount());
                refund.setPaymentMethod(payment.getPaymentMethod());
                refund.setTransactionId(generateTransactionId());
                refund.setPaymentStatus(PaymentStatus.REFUNDED);

                boolean saved = paymentDAO.save(refund);

                if (!saved) {
                    throw new PaymentException("Khong the hoan tien");
                }

                return true;
            });

        } catch (SQLException e) {
            throw new DatabaseException("Loi khi hoan tien", e);
        }
    }

    // Lay thong tin thanh toan
    public Payment getPaymentDetails(int paymentId) throws DatabaseException {
        try {
            Payment payment = paymentDAO.findById(paymentId);
            if (payment == null) {
                throw new DatabaseException("Khong tim thay thong tin thanh toan");
            }
            return payment;
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi tai thong tin thanh toan", e);
        }
    }

    // Sinh ma giao dich
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis();
    }

    // Gia lap cong thanh toan
    private boolean simulatePaymentGateway(Payment payment) {
        // Gia lap thanh cong 90%
        return Math.random() < 0.9;
    }
}