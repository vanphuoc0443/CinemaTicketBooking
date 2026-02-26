package fxcontroller;

import dao.BookingDAO;
import dao.SeatDAO;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Booking;
import model.BookingRecord;
import model.BookingStatus;
import model.Seat;
import util.Session;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class HistoryController {

    public static List<BookingRecord> bookingHistory = new ArrayList<>();

    @FXML
    private VBox historyContainer;
    @FXML
    private Label emptyLabel;

    public void initialize() {
        // Try loading from DB first
        loadFromDB();

        // Merge with in-memory records (avoid duplicates by checking already displayed)
        List<BookingRecord> allRecords = new ArrayList<>();

        // DB records first, then local-only (if any not in DB)
        allRecords.addAll(bookingHistory);

        if (allRecords.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
        } else {
            emptyLabel.setVisible(false);
            emptyLabel.setManaged(false);

            for (int i = allRecords.size() - 1; i >= 0; i--) {
                BookingRecord record = allRecords.get(i);
                historyContainer.getChildren().add(createBookingCard(record, allRecords.size() - i));
            }
        }
    }

    private void loadFromDB() {
        try {
            if (Session.getCurrentCustomer() == null)
                return;
            int customerId = Session.getCurrentCustomer().getCustomerId();

            BookingDAO bookingDAO = new BookingDAO();
            List<Booking> dbBookings = bookingDAO.findByCustomer(customerId);

            if (dbBookings != null && !dbBookings.isEmpty()) {
                SeatDAO seatDAO = new SeatDAO();

                // Check which DB bookings are already in local history (avoid duplicates)
                Set<String> existingKeys = new HashSet<>();
                for (BookingRecord r : bookingHistory) {
                    existingKeys.add(r.getMovieTitle() + "|" + r.getFormattedBookingTime());
                }

                for (Booking booking : dbBookings) {
                    if (booking.getStatus() == BookingStatus.CANCELLED)
                        continue;

                    // Get seat numbers
                    Set<String> seatNumbers = new LinkedHashSet<>();
                    if (booking.getSeatIds() != null && !booking.getSeatIds().isEmpty()) {
                        List<Seat> seats = seatDAO.findByIds(booking.getSeatIds());
                        for (Seat s : seats) {
                            seatNumbers.add(s.getSeatNumber());
                        }
                    }

                    String movieTitle = booking.getMovieTitle() != null
                            ? booking.getMovieTitle()
                            : "Phim #" + booking.getShowtimeId();
                    String showDate = booking.getShowDate() != null ? booking.getShowDate() : "—";
                    String showTime = booking.getShowTime() != null ? booking.getShowTime() : "—";

                    String key = movieTitle + "|" + (booking.getBookingTime() != null
                            ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(booking.getBookingTime())
                            : "");

                    if (!existingKeys.contains(key)) {
                        BookingRecord record = new BookingRecord(
                                movieTitle,
                                "Cinema ABC - Phòng 01",
                                showTime,
                                showDate,
                                seatNumbers.isEmpty() ? Set.of("—") : seatNumbers,
                                (int) booking.getTotalAmount());
                        bookingHistory.add(record);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠ Could not load booking history from DB: " + e.getMessage());
        }
    }

    private VBox createBookingCard(BookingRecord record, int index) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card-elevated");
        card.setPadding(new Insets(16, 20, 16, 20));

        // Header: index + movie title
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label("#" + index);
        badge.getStyleClass().add("chip");
        Label title = new Label("🎬 " + record.getMovieTitle());
        title.getStyleClass().add("heading-sm");
        header.getChildren().addAll(badge, title);

        Separator sep = new Separator();

        // Info grid
        HBox infoRow1 = new HBox(24);
        infoRow1.setAlignment(Pos.CENTER_LEFT);
        infoRow1.getChildren().addAll(
                infoBlock("🏛 Rạp", record.getCinema()),
                infoBlock("📅 Ngày chiếu", record.getDate()));

        HBox infoRow2 = new HBox(24);
        infoRow2.setAlignment(Pos.CENTER_LEFT);
        infoRow2.getChildren().addAll(
                infoBlock("🕐 Suất chiếu", record.getShowtime()),
                infoBlock("🎟 Ghế", record.getSeatList()));

        HBox infoRow3 = new HBox(24);
        infoRow3.setAlignment(Pos.CENTER_LEFT);
        infoRow3.getChildren().addAll(
                infoBlock("💰 Tổng tiền", record.getFormattedPrice()),
                infoBlock("🕒 Thời gian đặt", record.getFormattedBookingTime()));

        card.getChildren().addAll(header, sep, infoRow1, infoRow2, infoRow3);
        return card;
    }

    private VBox infoBlock(String label, String value) {
        VBox block = new VBox(2);
        block.setMinWidth(180);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("info-label");
        Label val = new Label(value);
        val.getStyleClass().add("info-value");
        val.setWrapText(true);
        block.getChildren().addAll(lbl, val);
        return block;
    }

    @FXML
    public void home() {
        SceneManager.switchScene("home.fxml");
    }
}
