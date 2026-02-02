package controller;

import dao.*;
import model.*;
import exception.*;
import util.IMDBApiClient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Enhanced Booking Controller với:
 * - Seat locking mechanism
 * - IMDB integration
 * - Complete booking flow
 */
public class EnhancedBookingController {

    private final BookingController bookingController;
    private final SeatLockDAO seatLockDAO;
    private final SeatDAO seatDAO;
    private final ShowtimeDAO showtimeDAO;
    private final MovieDAO movieDAO;
    private final PaymentDAO paymentDAO;

    public EnhancedBookingController() {
        this.bookingController = new BookingController();
        this.seatLockDAO = new SeatLockDAO();
        this.seatDAO = new SeatDAO();
        this.showtimeDAO = new ShowtimeDAO();
        this.movieDAO = new MovieDAO();
        this.paymentDAO = new PaymentDAO();
    }

    /**
     * STEP 1: Lock ghế khi user chọn
     * Trả về danh sách seat IDs đã lock thành công
     */
    public List<Integer> lockSeatsForBooking(List<Integer> seatIds, int showtimeId,
                                             int customerId, String sessionToken)
            throws BookingException, DatabaseException {

        try {
            // Validate showtime exists
            Showtime showtime = showtimeDAO.findById(showtimeId);
            if (showtime == null) {
                throw new BookingException("Suất chiếu không tồn tại");
            }

            // Validate seats exist and available
            List<Integer> validSeatIds = new ArrayList<>();
            for (int seatId : seatIds) {
                Seat seat = seatDAO.findById(seatId);
                if (seat == null) {
                    throw new BookingException("Ghế ID " + seatId + " không tồn tại");
                }

                // Check if seat is available for this showtime
                if (!isSeatAvailable(seatId, showtimeId, customerId)) {
                    throw new SeatUnavailableException("Ghế " + seat.getSeatNumber() + " không còn trống");
                }

                validSeatIds.add(seatId);
            }

            // Lock all seats
            boolean lockSuccess = seatLockDAO.lockSeats(validSeatIds, showtimeId,
                    customerId, sessionToken);

            if (!lockSuccess) {
                throw new BookingException("Không thể giữ ghế. Vui lòng thử lại.");
            }

            return validSeatIds;

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi database khi lock ghế: " + e.getMessage(), e);
        }
    }

    /**
     * STEP 2: Tạo booking từ locked seats
     * Chuyển từ temporary lock sang booking thực sự
     */
    public BookingResult createBookingFromLocks(int customerId, String sessionToken,
                                                int showtimeId, PaymentMethod paymentMethod)
            throws BookingException, DatabaseException {

        try {
            // 1. Get locked seats for this session
            List<SeatLock> locks = seatLockDAO.getUserLocks(sessionToken, showtimeId);

            if (locks.isEmpty()) {
                throw new BookingException("Không tìm thấy ghế đã chọn. Vui lòng chọn lại.");
            }

            // Check locks haven't expired
            for (SeatLock lock : locks) {
                if (lock.isExpired()) {
                    throw new BookingException("Ghế đã hết thời gian giữ. Vui lòng chọn lại.");
                }
            }

            // 2. Extract seat IDs
            List<Integer> seatIds = new ArrayList<>();
            for (SeatLock lock : locks) {
                seatIds.add(lock.getSeatId());
            }

            // 3. Calculate total amount
            double totalAmount = calculateTotalAmount(seatIds);

            // 4. Create booking using existing BookingController
            Booking booking = bookingController.createBooking(
                    customerId,
                    showtimeId,
                    seatIds,
                    totalAmount
            );

            if (booking == null) {
                throw new BookingException("Không thể tạo booking");
            }

            // 5. Process payment
            Payment payment = processPayment(booking.getBookingId(), paymentMethod, totalAmount);

            // 6. Convert locks to booking (deactivate locks)
            seatLockDAO.convertLocksToBooking(sessionToken, showtimeId, booking.getBookingId());

            // 7. Get complete booking info with movie details
            BookingResult result = buildBookingResult(booking, payment);

            return result;

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi database khi tạo booking: " + e.getMessage(), e);
        }
    }

    /**
     * Unlock ghế khi user bỏ chọn
     */
    public boolean unlockSeat(int seatId, int showtimeId, String sessionToken)
            throws DatabaseException {
        try {
            return seatLockDAO.unlockSeat(seatId, showtimeId, sessionToken);
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi unlock ghế: " + e.getMessage(), e);
        }
    }

    /**
     * Unlock tất cả ghế của user (khi rời trang)
     */
    public boolean unlockAllSeats(String sessionToken, int showtimeId)
            throws DatabaseException {
        try {
            return seatLockDAO.unlockAllSeatsForSession(sessionToken, showtimeId);
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi unlock ghế: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy trạng thái ghế cho suất chiếu
     * Bao gồm: available, locked (by others), booked, locked (by current user)
     */
    public List<SeatStatus> getSeatsStatus(int showtimeId, String currentSessionToken)
            throws DatabaseException {

        try {
            List<SeatStatus> seatStatuses = new ArrayList<>();

            // Get all seats
            List<Seat> allSeats = seatDAO.findAll();

            // Get booked seats
            List<Seat> bookedSeats = seatDAO.getBookedSeats(showtimeId);

            // Get locked seats
            List<SeatLock> lockedSeats = seatLockDAO.getLockedSeats(showtimeId);

            for (Seat seat : allSeats) {
                SeatStatus status = new SeatStatus();
                status.setSeat(seat);

                // Check if booked
                boolean isBooked = bookedSeats.stream()
                        .anyMatch(s -> s.getSeatId() == seat.getSeatId());

                if (isBooked) {
                    status.setStatus("booked");
                    seatStatuses.add(status);
                    continue;
                }

                // Check if locked
                SeatLock lock = lockedSeats.stream()
                        .filter(l -> l.getSeatId() == seat.getSeatId())
                        .findFirst()
                        .orElse(null);

                if (lock != null) {
                    if (lock.getSessionToken().equals(currentSessionToken)) {
                        status.setStatus("locked-mine");
                        status.setLockExpiry(lock.getExpiresAt());
                        status.setRemainingSeconds(lock.getRemainingSeconds());
                    } else {
                        status.setStatus("locked-others");
                    }
                } else {
                    status.setStatus("available");
                }

                seatStatuses.add(status);
            }

            return seatStatuses;

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi lấy trạng thái ghế: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy thông tin phim với IMDB data
     */
    public MovieWithRating getMovieWithIMDB(int movieId) throws DatabaseException {
        try {
            Movie movie = movieDAO.findById(movieId);
            if (movie == null) {
                return null;
            }

            // Get IMDB rating
            MovieRating imdbData = IMDBApiClient.getMovieRating(movie.getTitle());

            MovieWithRating result = new MovieWithRating();
            result.setMovie(movie);
            result.setImdbData(imdbData);

            return result;

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi lấy thông tin phim: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy lịch sử booking của user
     */
    public List<BookingResult> getUserBookingHistory(int customerId)
            throws DatabaseException {

        try {
            List<Booking> bookings = BookingController.getCustomerBookings(customerId);
            List<BookingResult> results = new ArrayList<>();

            for (Booking booking : bookings) {
                Payment payment = paymentDAO.findByBookingId(booking.getBookingId());
                BookingResult result = buildBookingResult(booking, payment);
                results.add(result);
            }

            return results;

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi lấy lịch sử booking: " + e.getMessage(), e);
        }
    }

    /**
     * Hủy booking (nếu chưa quá giờ chiếu)
     */
    public boolean cancelBooking(int bookingId, int customerId)
            throws BookingException, DatabaseException {

        try {
            Booking booking = bookingController.getBookingById(bookingId);

            if (booking == null) {
                throw new BookingException("Không tìm thấy booking");
            }

            if (booking.getCustomerId() != customerId) {
                throw new BookingException("Bạn không có quyền hủy booking này");
            }

            Showtime showtime = showtimeDAO.findById(booking.getShowtimeId());
            if (showtime == null) {
                throw new BookingException("Không tìm thấy suất chiếu");
            }

            // Check if showtime hasn't started (allow cancel up to 1 hour before)
            long now = System.currentTimeMillis();
            long showtimeStart = showtime.getShowDate().getTime();
            long oneHour = 60 * 60 * 1000;

            if (now >= (showtimeStart - oneHour)) {
                throw new BookingException("Không thể hủy vé trước giờ chiếu dưới 1 giờ");
            }

            // Cancel booking
            return bookingController.cancelBooking(bookingId);

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi hủy booking: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    private boolean isSeatAvailable(int seatId, int showtimeId, int customerId)
            throws SQLException {

        // Check if already booked
        List<Seat> bookedSeats = seatDAO.getBookedSeats(showtimeId);
        boolean isBooked = bookedSeats.stream()
                .anyMatch(s -> s.getSeatId() == seatId);

        if (isBooked) {
            return false;
        }

        // Check if locked by another user
        return !seatLockDAO.isSeatLocked(seatId, showtimeId, customerId);
    }

    private double calculateTotalAmount(List<Integer> seatIds) throws SQLException {
        double total = 0;
        for (int seatId : seatIds) {
            Seat seat = seatDAO.findById(seatId);
            if (seat != null) {
                total += seat.getPrice();
            }
        }
        return total;
    }

    private Payment processPayment(int bookingId, PaymentMethod paymentMethod, double amount)
            throws SQLException {

        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.COMPLETED); // Assume successful for demo
        payment.setPaymentDate(new Date());

        paymentDAO.save(payment);

        return payment;
    }

    private BookingResult buildBookingResult(Booking booking, Payment payment)
            throws SQLException {

        BookingResult result = new BookingResult();
        result.setBooking(booking);
        result.setPayment(payment);

        // Get showtime details
        Showtime showtime = showtimeDAO.findById(booking.getShowtimeId());
        result.setShowtime(showtime);

        // Get movie details
        if (showtime != null) {
            Movie movie = movieDAO.findById(showtime.getMovieId());
            result.setMovie(movie);

            // Optionally get IMDB data
            if (movie != null) {
                MovieRating imdbData = IMDBApiClient.getMovieRating(movie.getTitle());
                result.setImdbData(imdbData);
            }
        }

        // Get seat details
        List<Seat> seats = new ArrayList<>();
        // Note: You'll need to add a method in BookingSeatDAO to get seats by booking
        // For now, we'll leave this as TODO
        result.setSeats(seats);

        return result;
    }
}

/**
 * Helper class để trả về trạng thái ghế
 */
class SeatStatus {
    private Seat seat;
    private String status; // "available", "booked", "locked-mine", "locked-others"
    private java.sql.Timestamp lockExpiry;
    private long remainingSeconds;

    public Seat getSeat() { return seat; }
    public void setSeat(Seat seat) { this.seat = seat; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.sql.Timestamp getLockExpiry() { return lockExpiry; }
    public void setLockExpiry(java.sql.Timestamp lockExpiry) { this.lockExpiry = lockExpiry; }

    public long getRemainingSeconds() { return remainingSeconds; }
    public void setRemainingSeconds(long remainingSeconds) { this.remainingSeconds = remainingSeconds; }
}

/**
 * Helper class để trả về kết quả booking đầy đủ
 */
class BookingResult {
    private Booking booking;
    private Payment payment;
    private Showtime showtime;
    private Movie movie;
    private MovieRating imdbData;
    private List<Seat> seats;

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public Showtime getShowtime() { return showtime; }
    public void setShowtime(Showtime showtime) { this.showtime = showtime; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public MovieRating getImdbData() { return imdbData; }
    public void setImdbData(MovieRating imdbData) { this.imdbData = imdbData; }

    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }
}

/**
 * Helper class để trả về movie với IMDB data
 */
class MovieWithRating {
    private Movie movie;
    private MovieRating imdbData;

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public MovieRating getImdbData() { return imdbData; }
    public void setImdbData(MovieRating imdbData) { this.imdbData = imdbData; }
}