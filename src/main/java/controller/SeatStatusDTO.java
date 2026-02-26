package controller;

import model.SeatType;
import java.sql.Timestamp;

/**
 * DTO class để trả về trạng thái ghế
 */
public class SeatStatusDTO {
    private int seatId;
    private String seatNumber;
    private SeatType seatType;
    private double price;
    private String status; // "available", "booked", "locked-mine", "locked-others"
    private boolean available;
    private Timestamp lockExpiresAt;
    private long remainingSeconds;

    // Getters and Setters
    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Timestamp getLockExpiresAt() {
        return lockExpiresAt;
    }

    public void setLockExpiresAt(Timestamp lockExpiresAt) {
        this.lockExpiresAt = lockExpiresAt;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}
