package model;

import java.sql.Timestamp;

public class Showtime {
    private int showtimeId;
    private int movieId;
    private String showDate;
    private String showTime;
    private int roomNumber;
    private int totalSeats;
    private int availableSeats;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private String movieTitle;

    public Showtime() {
    }

    public Showtime(int showtimeId, int movieId, String showDate, String showTime, int roomNumber) {
        this.showtimeId = showtimeId;
        this.movieId = movieId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.roomNumber = roomNumber;
        this.totalSeats = 80;
        this.availableSeats = 80;
    }

    public Showtime(int showtimeId, int movieId, String showDate, String showTime,
            int roomNumber, int totalSeats, int availableSeats) {
        this.showtimeId = showtimeId;
        this.movieId = movieId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.roomNumber = roomNumber;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
    }

    public int getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(int showtimeId) {
        this.showtimeId = showtimeId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
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

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
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

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    @Override
    public String toString() {
        return "Showtime{" +
                "showtimeId=" + showtimeId +
                ", movieId=" + movieId +
                ", showDate='" + showDate + '\'' +
                ", showTime='" + showTime + '\'' +
                ", roomNumber=" + roomNumber +
                ", availableSeats=" + availableSeats +
                '}';
    }
}