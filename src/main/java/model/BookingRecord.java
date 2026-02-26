package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Stores detailed booking information for history display.
 */
public class BookingRecord {
    private String movieTitle;
    private String cinema;
    private String showtime;
    private String date;
    private Set<String> seats;
    private int totalPrice;
    private LocalDateTime bookingTime;

    public BookingRecord(String movieTitle, String cinema, String showtime,
            String date, Set<String> seats, int totalPrice) {
        this.movieTitle = movieTitle;
        this.cinema = cinema;
        this.showtime = showtime;
        this.date = date;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.bookingTime = LocalDateTime.now();
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getCinema() {
        return cinema;
    }

    public String getShowtime() {
        return showtime;
    }

    public String getDate() {
        return date;
    }

    public Set<String> getSeats() {
        return seats;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public String getFormattedPrice() {
        return String.format("%,dđ", totalPrice).replace(',', '.');
    }

    public String getFormattedBookingTime() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return bookingTime.format(fmt);
    }

    public String getSeatList() {
        return String.join(", ", seats);
    }
}
