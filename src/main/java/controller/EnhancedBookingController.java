package controller;

import dao.*;
import exception.BookingException;
import exception.DatabaseException;
import exception.SeatUnavailableException;
import model.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced Booking Controller với seat locking mechanism
 * ✅ FIXED: Compatible với existing DAO methods
 */
public class EnhancedBookingController {

    private final BookingController bookingController;
    private final SeatLockDAO seatLockDAO;
    private final SeatDAO seatDAO;
    private final ShowtimeDAO showtimeDAO;
    private final BookingDAO bookingDAO;

    public EnhancedBookingController(BookingController bookingController) {
        this.bookingController = bookingController;
        this.seatLockDAO = new SeatLockDAO();
        this.seatDAO = new SeatDAO();
        this.showtimeDAO = new ShowtimeDAO();
        this.bookingDAO = new BookingDAO();
    }

    /**
     * STEP 1: Lock ghế khi user chọn
     *
     * @throws SeatUnavailableException nếu ghế không còn trống
     */
    public List<Integer> lockSeatsForBooking(
            List<Integer> seatIds,
            int showtimeId,
            int customerId,
            String sessionToken) throws BookingException, DatabaseException, SeatUnavailableException {

        try {
            // Validate showtime exists
            Showtime showtime = showtimeDAO.findById(showtimeId);
            if (showtime == null) {
                throw new BookingException("Suất chiếu không tồn tại");
            }

            // Validate input
            if (seatIds == null || seatIds.isEmpty()) {
                throw new BookingException("Vui lòng chọn ít nhất 1 ghế");
            }

            // Validate each seat
            List<Seat> seats = seatDAO.findByIds(seatIds);

            // Check if all seats were found
            if (seats.size() != seatIds.size()) {
                throw new BookingException("Một số ghế không tồn tại");
            }

            List<Integer> validSeatIds = new ArrayList<>();
            for (Seat seat : seats) {
                // Check if seat belongs to this showtime
                if (seat.getShowtimeId() != showtimeId) {
                    throw new BookingException(
                            "Ghế " + seat.getSeatNumber() + " không thuộc suất chiếu này");
                }

                // Check if seat is available (using existing method)
                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new SeatUnavailableException(
                            seat.getSeatId(),
                            "Ghế " + seat.getSeatNumber() + " đã được đặt");
                }

                // NO NEED to check isSeatLocked here one by one.
                // It will be checked atomically in seatLockDAO.lockSeats

                validSeatIds.add(seat.getSeatId());
            }

            // Lock all seats
            boolean lockSuccess = seatLockDAO.lockSeats(
                    validSeatIds,
                    showtimeId,
                    customerId,
                    sessionToken);

            if (!lockSuccess) {
                throw new BookingException("Không thể giữ ghế. Vui lòng thử lại.");
            }

            return validSeatIds;

        } catch (SeatUnavailableException e) {
            throw e; // Re-throw as-is
        } catch (BookingException e) {
            throw e; // Re-throw as-is
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi database khi lock ghế: " + e.getMessage(), e);
        }
    }

    /**
     * STEP 2: Tạo booking từ locked seats
     */
    public Booking createBookingFromLocks(
            int customerId,
            String sessionToken,
            int showtimeId) throws BookingException, DatabaseException, SeatUnavailableException {

        try {
            // Get locked seats for this session
            List<SeatLock> locks = seatLockDAO.getUserLocks(sessionToken, showtimeId);

            if (locks.isEmpty()) {
                throw new BookingException("Không tìm thấy ghế đã chọn. Vui lòng chọn lại.");
            }

            // Check if any lock has expired
            for (SeatLock lock : locks) {
                if (lock.isExpired()) {
                    throw new BookingException(
                            "Thời gian giữ ghế đã hết. Vui lòng chọn lại.");
                }
            }

            // Extract seat IDs
            List<Integer> seatIds = new ArrayList<>();
            for (SeatLock lock : locks) {
                seatIds.add(lock.getSeatId());
            }

            // Create booking using existing BookingController
            Booking booking = bookingController.createBooking(
                    customerId,
                    showtimeId,
                    seatIds);

            if (booking == null) {
                throw new BookingException("Không thể tạo booking");
            }

            // Deactivate locks (convert to booking)
            boolean converted = seatLockDAO.convertLocksToBooking(
                    sessionToken,
                    showtimeId,
                    booking.getBookingId());

            if (!converted) {
                // Log warning but don't fail - booking already created
                System.err.println("Warning: Could not deactivate locks for booking " +
                        booking.getBookingId());
            }

            return booking;

        } catch (SeatUnavailableException e) {
            throw e;
        } catch (BookingException e) {
            throw e;
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi database khi tạo booking: " + e.getMessage(), e);
        }
    }

    /**
     * Unlock một ghế (khi user bỏ chọn)
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
     * Unlock tất cả ghế của session (khi user rời trang)
     */
    public boolean unlockAllSeats(String sessionToken, int showtimeId)
            throws DatabaseException {
        try {
            return seatLockDAO.unlockAllSeatsForSession(sessionToken, showtimeId);
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi unlock tất cả ghế: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy trạng thái ghế cho suất chiếu
     * Trả về: "available", "booked", "locked-mine", "locked-others"
     */
    public List<SeatStatusDTO> getSeatsStatus(int showtimeId, String currentSessionToken)
            throws DatabaseException {
        try {
            List<SeatStatusDTO> result = new ArrayList<>();

            // Get all seats for this showtime (using existing method)
            List<Seat> allSeats = seatDAO.findByShowtime(showtimeId);

            // Get locked seats
            List<SeatLock> activeLocks = seatLockDAO.getLockedSeats(showtimeId);

            for (Seat seat : allSeats) {
                SeatStatusDTO dto = new SeatStatusDTO();
                dto.setSeatId(seat.getSeatId());
                dto.setSeatNumber(seat.getSeatNumber());
                dto.setSeatType(seat.getSeatType());
                dto.setPrice(seat.getPrice());

                // Check seat status
                if (seat.getStatus() == SeatStatus.RESERVED ||
                        seat.getStatus() == SeatStatus.BOOKED) {
                    // Seat is booked
                    dto.setStatus("booked");
                    dto.setAvailable(false);

                } else {
                    // Check if locked
                    SeatLock lock = findLockForSeat(activeLocks, seat.getSeatId());

                    if (lock != null && !lock.isExpired()) {
                        if (lock.getSessionToken().equals(currentSessionToken)) {
                            // Locked by current user
                            dto.setStatus("locked-mine");
                            dto.setAvailable(false);
                            dto.setLockExpiresAt(lock.getExpiresAt());
                            dto.setRemainingSeconds(lock.getRemainingSeconds());
                        } else {
                            // Locked by another user
                            dto.setStatus("locked-others");
                            dto.setAvailable(false);
                        }
                    } else {
                        // Available
                        dto.setStatus("available");
                        dto.setAvailable(true);
                    }
                }

                result.add(dto);
            }

            return result;

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi lấy trạng thái ghế: " + e.getMessage(), e);
        }
    }

    /**
     * Helper: Tìm lock cho một seat
     */
    private SeatLock findLockForSeat(List<SeatLock> locks, int seatId) {
        for (SeatLock lock : locks) {
            if (lock.getSeatId() == seatId) {
                return lock;
            }
        }
        return null;
    }

    /**
     * Hủy booking
     * Chỉ cho phép hủy nếu còn hơn 1 giờ trước giờ chiếu
     */
    public boolean cancelBooking(int bookingId, int customerId)
            throws BookingException, DatabaseException {
        try {
            // Get booking (using existing DAO method)
            Booking booking = bookingDAO.findById(bookingId);

            if (booking == null) {
                throw new BookingException("Không tìm thấy booking");
            }

            // Verify ownership
            if (booking.getCustomerId() != customerId) {
                throw new BookingException("Bạn không có quyền hủy booking này");
            }

            // Check booking status
            if (booking.getStatus() != BookingStatus.PENDING &&
                    booking.getStatus() != BookingStatus.CONFIRMED) {
                throw new BookingException("Booking không thể hủy (trạng thái: " +
                        booking.getStatus() + ")");
            }

            // Get showtime to check time
            Showtime showtime = showtimeDAO.findById(booking.getShowtimeId());
            if (showtime == null) {
                throw new BookingException("Không tìm thấy suất chiếu");
            }

            // Check time: must be at least 1 hour before showtime
            long currentTime = System.currentTimeMillis();

            // Combine show_date and show_time
            Timestamp showtimeTimestamp = combineDateTime(
                    showtime.getShowDate(),
                    showtime.getShowTime());
            long showtimeMillis = showtimeTimestamp.getTime();

            long oneHourInMillis = 60 * 60 * 1000;

            if (currentTime >= (showtimeMillis - oneHourInMillis)) {
                throw new BookingException(
                        "Không thể hủy vé trong vòng 1 giờ trước giờ chiếu");
            }

            // Cancel using existing method (update status to CANCELLED)
            boolean cancelled = bookingDAO.updateStatus(bookingId, BookingStatus.CANCELLED);

            if (cancelled) {
                // Release seats back to AVAILABLE
                for (int seatId : booking.getSeatIds()) {
                    seatDAO.updateSeatStatus(seatId, SeatStatus.AVAILABLE);
                }
            }

            return cancelled;

        } catch (BookingException e) {
            throw e;
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi hủy booking: " + e.getMessage(), e);
        }
    }

    /**
     * Helper: Combine date and time into Timestamp
     */
    /**
     * Helper: Combine date and time strings into Timestamp
     */
    private Timestamp combineDateTime(String dateStr, String timeStr) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date date = sdf.parse(dateStr + " " + timeStr);
            return new Timestamp(date.getTime());
        } catch (java.text.ParseException e) {
            // Fallback or throw runtime exception
            throw new RuntimeException("Invalid date/time format: " + dateStr + " " + timeStr, e);
        }
    }
}
