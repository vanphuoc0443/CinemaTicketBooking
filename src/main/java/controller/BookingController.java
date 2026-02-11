package controller;

import dao.BookingDAO;
import dao.SeatDAO;
import dao.ShowtimeDAO;
import dao.TransactionManager;
import model.Booking;
import model.Seat;
import model.Showtime;
import model.SeatStatus;
import model.BookingStatus;
import exception.BookingException;
import exception.SeatUnavailableException;
import exception.CancellationException;
import exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller xử lý logic nghiệp vụ cho booking
 * ✅ Đã sửa lỗi logic và cải thiện error handling
 */
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private BookingDAO bookingDAO;
    private SeatDAO seatDAO;
    private ShowtimeDAO showtimeDAO;
    private TransactionManager transactionManager;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 100;
    private static final int CANCELLATION_HOURS_LIMIT = 2;
    private static final int MAX_SEATS_PER_BOOKING = 10;

    public BookingController(BookingDAO bookingDAO,
                             SeatDAO seatDAO,
                             ShowtimeDAO showtimeDAO,
                             TransactionManager transactionManager) {
        this.bookingDAO = bookingDAO;
        this.seatDAO = seatDAO;
        this.showtimeDAO = showtimeDAO;
        this.transactionManager = transactionManager;
    }

    /**
     * Tạo đơn đặt vé với transaction và retry logic
     */
    public Booking createBooking(int customerId, int showtimeId, List<Integer> seatIds)
            throws BookingException, SeatUnavailableException, DatabaseException {

        logger.info("Creating booking for customer {} showtime {} seats {}",
                customerId, showtimeId, seatIds);

        validateBookingInput(customerId, showtimeId, seatIds);

        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                Booking booking = attemptCreateBooking(customerId, showtimeId, seatIds);
                logger.info("Successfully created booking {}", booking.getBookingId());
                return booking;

            } catch (SQLException e) {
                lastException = e;
                attempts++;

                logger.warn("Attempt {} failed: {}", attempts, e.getMessage());

                if (isRetryableException(e) && attempts < MAX_RETRY_ATTEMPTS) {
                    try {
                        long delay = RETRY_DELAY_MS * attempts;
                        logger.info("Retrying in {}ms...", delay);
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new DatabaseException("Bị gián đoạn khi thử lại", ie);
                    }
                } else {
                    break;
                }
            }
        }

        logger.error("Failed to create booking after {} attempts", attempts);
        throw new DatabaseException(
                "Không thể tạo đơn đặt vé sau " + attempts + " lần thử",
                (Exception) lastException
        );
    }

    /**
     * Thực hiện tạo booking trong transaction
     */
    private Booking attemptCreateBooking(int customerId, int showtimeId, List<Integer> seatIds)
            throws SQLException, SeatUnavailableException, BookingException {

        return (Booking) transactionManager.executeInTransaction(() -> {
            // Lock tất cả ghế cần đặt
            List<Seat> seats = new ArrayList<>();
            for (int seatId : seatIds) {
                Seat seat = seatDAO.lockSeatForUpdate(seatId);

                if (seat == null) {
                    throw new BookingException("Ghế không tồn tại: " + seatId);
                }

                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new SeatUnavailableException(
                            seatId,
                            "Ghế " + seat.getSeatNumber() + " đã được đặt"
                    );
                }

                if (seat.getShowtimeId() != showtimeId) {
                    throw new BookingException(
                            "Ghế không thuộc suất chiếu này: " + seat.getSeatNumber()
                    );
                }

                seats.add(seat);
            }

            // Tính tổng tiền
            double totalAmount = calculateTotalAmount(seats);

            // Tạo booking
            Booking booking = new Booking();
            booking.setCustomerId(customerId);
            booking.setShowtimeId(showtimeId);
            booking.setSeatIds(seatIds);
            booking.setTotalAmount(totalAmount);
            booking.setStatus(BookingStatus.PENDING);
            booking.setBookingTime(new java.sql.Timestamp(System.currentTimeMillis()));

            int bookingId = bookingDAO.save(booking);
            booking.setBookingId(bookingId);

            // Cập nhật trạng thái ghế sang RESERVED
            for (int seatId : seatIds) {
                boolean updated = seatDAO.updateSeatStatus(seatId, SeatStatus.RESERVED);
                if (!updated) {
                    throw new BookingException("Không thể đặt ghế: " + seatId);
                }
            }

            return booking;
        });
    }

    /**
     * Xác nhận đơn đặt vé (sau khi thanh toán)
     */
    public boolean confirmBooking(int bookingId)
            throws BookingException, DatabaseException {

        logger.info("Confirming booking {}", bookingId);

        try {
            return (boolean) transactionManager.executeInTransaction(() -> {
                Booking booking = bookingDAO.findById(bookingId);

                if (booking == null) {
                    throw new BookingException("Không tìm thấy đơn đặt vé");
                }

                if (booking.getStatus() != BookingStatus.PENDING) {
                    throw new BookingException(
                            "Đơn đặt vé không ở trạng thái chờ xác nhận"
                    );
                }

                boolean bookingUpdated = bookingDAO.updateStatus(
                        bookingId,
                        BookingStatus.CONFIRMED
                );

                if (!bookingUpdated) {
                    throw new BookingException("Không thể xác nhận đơn đặt vé");
                }

                for (int seatId : booking.getSeatIds()) {
                    boolean seatUpdated = seatDAO.updateSeatStatus(
                            seatId,
                            SeatStatus.BOOKED
                    );

                    if (!seatUpdated) {
                        throw new BookingException(
                                "Không thể xác nhận ghế: " + seatId
                        );
                    }
                }

                logger.info("Successfully confirmed booking {}", bookingId);
                return true;
            });

        } catch (SQLException e) {
            logger.error("Error confirming booking {}", bookingId, e);
            throw new DatabaseException("Lỗi khi xác nhận đơn đặt vé", e);
        }
    }

    /**
     * Hủy đơn đặt vé
     */
    public boolean cancelBooking(int bookingId, String reason)
            throws CancellationException, DatabaseException {

        logger.info("Cancelling booking {} with reason: {}", bookingId, reason);

        try {
            return (boolean) transactionManager.executeInTransaction(() -> {
                Booking booking = bookingDAO.findById(bookingId);

                if (booking == null) {
                    throw new CancellationException(
                            bookingId,
                            "Không tìm thấy đơn đặt vé"
                    );
                }

                if (booking.getStatus() == BookingStatus.CANCELLED) {
                    throw new CancellationException(
                            bookingId,
                            "Đơn đặt vé đã được hủy trước đó"
                    );
                }

                // ✅ SỬA LỖI: Implement logic kiểm tra thời gian hủy vé
                if (!canCancelBooking(booking)) {
                    throw new CancellationException(
                            bookingId,
                            "Không thể hủy vé trong vòng " + CANCELLATION_HOURS_LIMIT +
                                    " giờ trước suất chiếu"
                    );
                }

                // Set cancellation reason
                booking.setCancellationReason(reason);
                booking.setCancelledAt(new java.sql.Timestamp(System.currentTimeMillis()));

                boolean bookingUpdated = bookingDAO.updateStatus(
                        bookingId,
                        BookingStatus.CANCELLED
                );

                if (!bookingUpdated) {
                    throw new CancellationException(
                            bookingId,
                            "Không thể hủy đơn đặt vé"
                    );
                }

                // Release all seats
                for (int seatId : booking.getSeatIds()) {
                    boolean seatReleased = seatDAO.releaseSeat(seatId);

                    if (!seatReleased) {
                        throw new CancellationException(
                                bookingId,
                                "Không thể giải phóng ghế: " + seatId
                        );
                    }
                }

                logger.info("Successfully cancelled booking {}", bookingId);
                return true;
            });

        } catch (SQLException e) {
            logger.error("Error cancelling booking {}", bookingId, e);
            throw new DatabaseException("Lỗi khi hủy đơn đặt vé", e);
        }
    }

    /**
     * Lấy chi tiết đơn đặt vé
     */
    public Booking getBookingDetails(int bookingId) throws DatabaseException {
        try {
            Booking booking = bookingDAO.findById(bookingId);
            if (booking == null) {
                throw new DatabaseException("Không tìm thấy đơn đặt vé");
            }
            return booking;
        } catch (SQLException e) {
            logger.error("Error getting booking details for {}", bookingId, e);
            throw new DatabaseException("Lỗi khi tải thông tin đơn đặt vé", e);
        }
    }

    /**
     * Lấy lịch sử đặt vé của khách hàng
     */
    public List<Booking> getBookingHistory(int customerId) throws DatabaseException {
        try {
            return bookingDAO.findByCustomer(customerId);
        } catch (SQLException e) {
            logger.error("Error getting booking history for customer {}", customerId, e);
            throw new DatabaseException("Không thể tải lịch sử đặt vé", e);
        }
    }

    /**
     * Validate input khi tạo booking
     */
    private void validateBookingInput(int customerId, int showtimeId, List<Integer> seatIds)
            throws BookingException {

        if (customerId <= 0) {
            throw new BookingException("ID khách hàng không hợp lệ");
        }

        if (showtimeId <= 0) {
            throw new BookingException("ID suất chiếu không hợp lệ");
        }

        if (seatIds == null || seatIds.isEmpty()) {
            throw new BookingException("Phải chọn ít nhất một ghế");
        }

        if (seatIds.size() > MAX_SEATS_PER_BOOKING) {
            throw new BookingException(
                    "Không thể đặt quá " + MAX_SEATS_PER_BOOKING + " ghế cùng lúc"
            );
        }

        // Check for duplicate seat IDs
        if (seatIds.size() != seatIds.stream().distinct().count()) {
            throw new BookingException("Danh sách ghế có ID trùng lặp");
        }
    }

    /**
     * Validate danh sách ghế
     */
    public boolean validateSeats(List<Integer> seatIds) throws DatabaseException {
        try {
            for (int seatId : seatIds) {
                Seat seat = seatDAO.findById(seatId);
                if (seat == null) {
                    return false;
                }
                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            logger.error("Error validating seats", e);
            throw new DatabaseException("Lỗi khi kiểm tra ghế", e);
        }
    }

    /**
     * Tính tổng tiền
     */
    private double calculateTotalAmount(List<Seat> seats) {
        double total = 0;
        for (Seat seat : seats) {
            total += seat.getPrice();
        }
        logger.debug("Calculated total amount: {}", total);
        return total;
    }

    /**
     * ✅ SỬA LỖI: Implement logic kiểm tra thời gian hủy vé
     * Kiểm tra có thể hủy booking không
     *
     * @param booking Booking cần kiểm tra
     * @return true nếu có thể hủy, false nếu không
     */
    private boolean canCancelBooking(Booking booking) {
        try {
            // Lấy thông tin suất chiếu
            Showtime showtime = showtimeDAO.findById(booking.getShowtimeId());

            if (showtime == null) {
                logger.error("Showtime not found for booking {}", booking.getBookingId());
                return false;
            }

            // Tính thời gian từ bây giờ đến suất chiếu
            LocalDateTime showtimeDateTime = LocalDateTime.of(
                    LocalDate.parse(showtime.getShowDate()),
                    LocalTime.parse(showtime.getShowTime())
            );

            LocalDateTime now = LocalDateTime.now();
            long hoursUntilShowtime = ChronoUnit.HOURS.between(now, showtimeDateTime);

            logger.debug("Hours until showtime: {}", hoursUntilShowtime);

            // Chỉ cho phép hủy nếu còn hơn CANCELLATION_HOURS_LIMIT giờ
            boolean canCancel = hoursUntilShowtime > CANCELLATION_HOURS_LIMIT;

            if (!canCancel) {
                logger.info("Cannot cancel booking {} - only {} hours until showtime",
                        booking.getBookingId(), hoursUntilShowtime);
            }

            return canCancel;

        } catch (SQLException e) {
            logger.error("Error checking cancellation policy for booking {}",
                    booking.getBookingId(), e);
            return false;
        }
    }

    /**
     * Kiểm tra exception có thể retry không
     *
     * @param e SQLException cần kiểm tra
     * @return true nếu có thể retry
     */
    private boolean isRetryableException(SQLException e) {
        String sqlState = e.getSQLState();
        int errorCode = e.getErrorCode();

        // MySQL error codes:
        // 1213 = Deadlock
        // 1205 = Lock wait timeout
        // 40xxx = Transaction rollback errors
        boolean retryable = errorCode == 1213 ||
                errorCode == 1205 ||
                (sqlState != null && sqlState.startsWith("40"));

        if (retryable) {
            logger.warn("Retryable exception detected: code={}, state={}",
                    errorCode, sqlState);
        }

        return retryable;
    }
}