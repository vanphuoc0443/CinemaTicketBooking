package fxcontroller;

import controller.BookingController;
import controller.EnhancedBookingController;
import dao.*;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import model.Booking;
import model.BookingRecord;
import model.Movie;
import model.SeatStatus;
import util.BookingSession;
import util.Session;

import java.util.LinkedHashSet;
import java.util.List;

public class PaymentController {

    @FXML
    private ComboBox<String> methodBox;
    @FXML
    private Label totalLabel;
    @FXML
    private Label moviePayLabel;
    @FXML
    private Label seatsPayLabel;

    public void initialize() {
        methodBox.getItems().addAll("Thẻ tín dụng", "Ví Momo", "ZaloPay", "VNPay");
        methodBox.getSelectionModel().selectFirst();

        int total = SeatController.totalPrice;
        totalLabel.setText(String.format("%,dđ", total).replace(',', '.'));

        Movie movie = HomeController.selectedMovie;
        moviePayLabel.setText(movie != null ? movie.getTitle() : "—");
        seatsPayLabel.setText(String.join(", ", SeatController.selectedSeats));
    }

    @FXML
    public void pay() {
        Movie movie = HomeController.selectedMovie;
        String movieTitle = movie != null ? movie.getTitle() : "Không rõ";
        String date = MovieDetailController.selectedDate != null ? MovieDetailController.selectedDate : "—";
        String time = MovieDetailController.selectedShowtime != null ? MovieDetailController.selectedShowtime : "—";

        // ====== 1. Try DB booking via seat_locks pipeline ======
        boolean dbBookingDone = false;
        try {
            BookingController bc = new BookingController(
                    new BookingDAO(), new SeatDAO(), new ShowtimeDAO(), new TransactionManager());
            EnhancedBookingController enhanced = new EnhancedBookingController(bc);

            int customerId = Session.getCurrentCustomer() != null
                    ? Session.getCurrentCustomer().getCustomerId()
                    : 0;

            if (customerId > 0 && BookingSession.getSessionToken() != null) {
                Booking booking = enhanced.createBookingFromLocks(
                        customerId,
                        BookingSession.getSessionToken(),
                        BookingSession.getShowtimeId());

                if (booking != null) {
                    dbBookingDone = true;
                    System.out.println("✅ Booking #" + booking.getBookingId() + " created via locks");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠ createBookingFromLocks failed: " + e.getMessage());
        }

        // ====== 2. Fallback: directly mark seats as BOOKED ======
        if (!dbBookingDone) {
            List<Integer> seatIds = BookingSession.getSelectedSeatIds();
            if (!seatIds.isEmpty()) {
                try {
                    SeatDAO seatDAO = new SeatDAO();
                    for (int seatId : seatIds) {
                        seatDAO.updateSeatStatus(seatId, SeatStatus.BOOKED);
                    }
                    System.out.println("✅ " + seatIds.size() + " seats marked BOOKED directly");
                } catch (Exception e) {
                    System.err.println("⚠ Direct seat update failed: " + e.getMessage());
                }
            }
        }

        // ====== 3. Local history ======
        BookingRecord record = new BookingRecord(
                movieTitle,
                "Cinema ABC - Phòng 01",
                time,
                date,
                new LinkedHashSet<>(SeatController.selectedSeats),
                SeatController.totalPrice);

        HistoryController.bookingHistory.add(record);

        // ====== 4. Cleanup ======
        BookingSession.clear();
        SceneManager.switchScene("ticket.fxml");
    }
}
