package controller;

import dao.BookingDAO;
import dao.SeatDAO;
import dao.TransactionManager;
import model.Booking;
import model.Seat;
import model.SeatStatus;
import model.BookingStatus;
import exception.BookingException;
import exception.SeatUnavailableException;
import exception.CancellationException;
import exception.DatabaseException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookingController {
    private BookingDAO bookingDAO;
    private SeatDAO seatDAO;
    private TransactionManager transactionManager;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 100;

    public BookingController(BookingDAO bookingDAO, SeatDAO seatDAO,
                             TransactionManager transactionManager) {
        this.bookingDAO = bookingDAO;
        this.seatDAO = seatDAO;
        this.transactionManager = transactionManager;
    }

    // Tao don dat ve voi transaction va retry logic
    public Booking createBooking(int customerId, int showtimeId, List<Integer> seatIds)
            throws BookingException, SeatUnavailableException, DatabaseException {

        validateBookingInput(customerId, showtimeId, seatIds);

        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                return attemptCreateBooking(customerId, showtimeId, seatIds);
            } catch (SQLException e) {
                lastException = e;
                attempts++;

                if (isRetryableException(e) && attempts < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new DatabaseException("Bi gian doan khi thu lai", ie);
                    }
                } else {
                    break;
                }
            }
        }

        throw new DatabaseException("Khong the tao don dat ve sau " + attempts + " lan thu",
                (Exception) lastException);
    }

    // Thuc hien tao booking trong transaction
    private Booking attemptCreateBooking(int customerId, int showtimeId, List<Integer> seatIds)
            throws SQLException, SeatUnavailableException, BookingException {

        return (Booking) transactionManager.executeInTransaction(() -> {
            // Lock tat ca ghe can dat
            List<Seat> seats = new ArrayList<>();
            for (int seatId : seatIds) {
                Seat seat = seatDAO.lockSeatForUpdate(seatId);

                if (seat == null) {
                    throw new BookingException("Ghe khong ton tai: " + seatId);
                }

                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new SeatUnavailableException(
                            seatId,
                            "Ghe " + seat.getSeatNumber() + " da duoc dat"
                    );
                }

                if (seat.getShowtimeId() != showtimeId) {
                    throw new BookingException(
                            "Ghe khong thuoc suat chieu nay: " + seat.getSeatNumber()
                    );
                }

                seats.add(seat);
            }

            // Tinh tong tien
            double totalAmount = calculateTotalAmount(seats);

            // Tao booking
            Booking booking = new Booking();
            booking.setCustomerId(customerId);
            booking.setShowtimeId(showtimeId);
            booking.setSeatIds(seatIds);
            booking.setTotalAmount(totalAmount);
            booking.setStatus(BookingStatus.PENDING);
            booking.setBookingTime(new java.sql.Timestamp(System.currentTimeMillis()));

            int bookingId = bookingDAO.save(booking);
            booking.setBookingId(bookingId);

            // Cap nhat trang thai ghe sang RESERVED
            for (int seatId : seatIds) {
                boolean updated = seatDAO.updateSeatStatus(seatId, SeatStatus.RESERVED);
                if (!updated) {
                    throw new BookingException("Khong the dat ghe: " + seatId);
                }
            }

            return booking;
        });
    }

    // Xac nhan don dat ve (sau khi thanh toan)
    public boolean confirmBooking(int bookingId)
            throws BookingException, DatabaseException {

        try {
            return (boolean) transactionManager.executeInTransaction(() -> {
                Booking booking = bookingDAO.findById(bookingId);

                if (booking == null) {
                    throw new BookingException("Khong tim thay don dat ve");
                }

                if (booking.getStatus() != BookingStatus.PENDING) {
                    throw new BookingException(
                            "Don dat ve khong o trang thai cho xac nhan"
                    );
                }

                boolean bookingUpdated = bookingDAO.updateStatus(
                        bookingId,
                        BookingStatus.CONFIRMED
                );

                if (!bookingUpdated) {
                    throw new BookingException("Khong the xac nhan don dat ve");
                }

                for (int seatId : booking.getSeatIds()) {
                    boolean seatUpdated = seatDAO.updateSeatStatus(
                            seatId,
                            SeatStatus.BOOKED
                    );

                    if (!seatUpdated) {
                        throw new BookingException(
                                "Khong the xac nhan ghe: " + seatId
                        );
                    }
                }

                return true;
            });

        } catch (SQLException e) {
            throw new DatabaseException("Loi khi xac nhan don dat ve", e);
        }
    }

    // Huy don dat ve
    public boolean cancelBooking(int bookingId)
            throws CancellationException, DatabaseException {

        try {
            return (boolean) transactionManager.executeInTransaction(() -> {
                Booking booking = bookingDAO.findById(bookingId);

                if (booking == null) {
                    throw new CancellationException(
                            bookingId,
                            "Khong tim thay don dat ve"
                    );
                }

                if (booking.getStatus() == BookingStatus.CANCELLED) {
                    throw new CancellationException(
                            bookingId,
                            "Don dat ve da duoc huy truoc do"
                    );
                }

                if (!canCancelBooking(booking)) {
                    throw new CancellationException(
                            bookingId,
                            "Khong the huy ve trong vong 2 gio truoc suat chieu"
                    );
                }

                boolean bookingUpdated = bookingDAO.updateStatus(
                        bookingId,
                        BookingStatus.CANCELLED
                );

                if (!bookingUpdated) {
                    throw new CancellationException(
                            bookingId,
                            "Khong the huy don dat ve"
                    );
                }

                for (int seatId : booking.getSeatIds()) {
                    boolean seatReleased = seatDAO.releaseSeat(seatId);

                    if (!seatReleased) {
                        throw new CancellationException(
                                bookingId,
                                "Khong the giai phong ghe: " + seatId
                        );
                    }
                }

                return true;
            });

        } catch (SQLException e) {
            throw new DatabaseException("Loi khi huy don dat ve", e);
        }
    }

    // Lay chi tiet don dat ve
    public Booking getBookingDetails(int bookingId) throws DatabaseException {
        try {
            Booking booking = bookingDAO.findById(bookingId);
            if (booking == null) {
                throw new DatabaseException("Khong tim thay don dat ve");
            }
            return booking;
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi tai thong tin don dat ve", e);
        }
    }

    // Lay lich su dat ve cua khach hang
    public List<Booking> getBookingHistory(int customerId) throws DatabaseException {
        try {
            return bookingDAO.findByCustomer(customerId);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the tai lich su dat ve", e);
        }
    }

    // Validate input khi tao booking
    private void validateBookingInput(int customerId, int showtimeId, List<Integer> seatIds)
            throws BookingException {

        if (customerId <= 0) {
            throw new BookingException("ID khach hang khong hop le");
        }

        if (showtimeId <= 0) {
            throw new BookingException("ID suat chieu khong hop le");
        }

        if (seatIds == null || seatIds.isEmpty()) {
            throw new BookingException("Phai chon it nhat mot ghe");
        }

        if (seatIds.size() > 10) {
            throw new BookingException("Khong the dat qua 10 ghe cung luc");
        }

        if (seatIds.size() != seatIds.stream().distinct().count()) {
            throw new BookingException("Danh sach ghe co ID trung lap");
        }
    }

    // Validate danh sach ghe
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
            throw new DatabaseException("Loi khi kiem tra ghe", e);
        }
    }

    // Tinh tong tien
    private double calculateTotalAmount(List<Seat> seats) {
        double total = 0;
        for (Seat seat : seats) {
            total += seat.getPrice();
        }
        return total;
    }

    // Kiem tra co the huy booking khong
    private boolean canCancelBooking(Booking booking) {
        // TODO: Implement logic kiem tra thoi gian
        return true;
    }

    // Kiem tra exception co the retry khong
    private boolean isRetryableException(SQLException e) {
        String sqlState = e.getSQLState();
        int errorCode = e.getErrorCode();

        return errorCode == 1213 || errorCode == 1205 ||
                (sqlState != null && sqlState.startsWith("40"));
    }
}