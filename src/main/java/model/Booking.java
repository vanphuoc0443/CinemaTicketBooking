package model;

import java.sql.Timestamp;
import java.util.*;

public class Booking {
    private int bookingId;
    private int customerId;
    private int showtimeId;
    private List<Integer> seatIds;
    private double totalAmount;
    private BookingStatus status;
    private Timestamp bookingTime;
    private Timestamp confirmedAt;
    private Timestamp cancelledAt;
    private String cancellationReason;

    private String customerName;
    private String movieTitle;
    private String showDate;
    private String showTime;

    public Booking() {
        this.seatIds = new ArrayList<>();
    }

    public Booking(int bookingId, int customerId, int showtimeId,
                   double totalAmount, BookingStatus status) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.showtimeId = showtimeId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.seatIds = new ArrayList<>();
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(int showtimeId) {
        this.showtimeId = showtimeId;
    }

    public List<Integer> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Integer> seatIds) {
        this.seatIds = seatIds;
    }

    public void addSeatId(int seatId) {
        if (this.seatIds == null) {
            this.seatIds = new ArrayList<>();
        }
        this.seatIds.add(seatId);
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Timestamp getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(Timestamp bookingTime) {
        this.bookingTime = bookingTime;
    }

    public Timestamp getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Timestamp confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Timestamp getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Timestamp cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public void confirm() {
        if (status == BookingStatus.PENDING) {
            status = BookingStatus.CONFIRMED;
            confirmedAt = new Timestamp(System.currentTimeMillis());
        }
    }

    public void cancel() {
        if (status.canBeCancelled()) {
            status = BookingStatus.CANCELLED;
            cancelledAt = new Timestamp(System.currentTimeMillis());
        }
    }

    public double calculateTotal(List<Seat> seats) {
        double total = 0;
        for (Seat seat : seats) {
            total += seat.getPrice();
        }
        this.totalAmount = total;
        return total;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", customerId=" + customerId +
                ", showtimeId=" + showtimeId +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", seatIds=" + seatIds +
                '}';
    }
}