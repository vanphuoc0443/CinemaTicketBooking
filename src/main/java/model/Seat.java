package model;

import java.sql.Timestamp;
import java.util.Date;

public class Seat {
    private int seatId;
    private int showtimeId;
    private String seatNumber;
    private SeatType seatType;
    private SeatStatus status;
    private double price;
    private int version;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Seat() {}

    public Seat(int seatId, int showtimeId, String seatNumber,
                SeatType seatType, SeatStatus status, int version) {
        this.seatId = seatId;
        this.showtimeId = showtimeId;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.status = status;
        this.price = seatType.getPrice();
        this.version = version;
    }

    public Seat(int seatId, int showtimeId, String seatNumber,
                SeatType seatType, SeatStatus status, double price, int version) {
        this.seatId = seatId;
        this.showtimeId = showtimeId;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.status = status;
        this.price = price;
        this.version = version;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(int showtimeId) {
        this.showtimeId = showtimeId;
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
        this.price = seatType.getPrice();
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    public void reserve() {
        if (status == SeatStatus.AVAILABLE) {
            status = SeatStatus.RESERVED;
        }
    }

    public void book() {
        if (status == SeatStatus.AVAILABLE || status == SeatStatus.RESERVED) {
            status = SeatStatus.BOOKED;
        }
    }

    public void release() {
        status = SeatStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "seatId=" + seatId +
                ", seatNumber='" + seatNumber + '\'' +
                ", seatType=" + seatType +
                ", status=" + status +
                ", price=" + price +
                ", version=" + version +
                '}';
    }
}