package fxcontroller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;

public class SummaryController {

    @FXML private ImageView imgPoster;
    @FXML private Label lblMovieTitle;
    @FXML private Label lblShowtime;
    @FXML private Label lblCinema;
    @FXML private Label lblSeats;
    @FXML private Label lblTicketCount;
    @FXML private Label lblCombo;
    @FXML private Label lblTicketPrice;
    @FXML private Label lblFee;
    @FXML private Label lblTotal;

    // Dữ liệu truyền từ màn trước (SeatController)
    private int movieId = 1;
    private int showtimeId = 1;
    private String selectedSeats = "A1, A2";
    private int ticketPrice = 75000; // giá 1 vé

    @FXML
    public void initialize() {
        loadMovieAndShowtime();
        calculatePrice();
    }

    private void loadMovieAndShowtime() {
        String sql = """
            SELECT m.title, m.poster_url, 
                   sh.show_time, sh.room_name, c.name AS cinema_name
            FROM showtimes sh
            JOIN movies m ON sh.movie_id = m.movie_id
            JOIN cinemas c ON sh.cinema_id = c.cinema_id
            WHERE sh.showtime_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                lblMovieTitle.setText(rs.getString("title"));
                lblShowtime.setText(rs.getString("show_time") + " | " + rs.getString("room_name"));
                lblCinema.setText(rs.getString("cinema_name"));

                String poster = rs.getString("poster_url");
                imgPoster.setImage(new Image(poster, true));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        lblSeats.setText(selectedSeats);
        lblTicketCount.setText(String.valueOf(selectedSeats.split(",").length));
    }

    private void calculatePrice() {
        int ticketCount = selectedSeats.split(",").length;
        int ticketsTotal = ticketCount * ticketPrice;
        int fee = 5000;
        int total = ticketsTotal + fee;

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        lblTicketPrice.setText(formatter.format(ticketsTotal) + " VND");
        lblFee.setText(formatter.format(fee) + " VND");
        lblTotal.setText(formatter.format(total) + " VND");
    }

    @FXML
    private void handlePayment() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/view/payment.fxml")
            );
            Parent root = loader.load();

            PaymentController paymentController = loader.getController();

            // Truyền data từ Summary sang Payment (chuẩn flow app cinema)
            paymentController.setPaymentData(
                    lblMovieTitle.getText(),
                    lblShowtime.getText(),
                    lblCinema.getText(),
                    selectedSeats,
                    lblCombo.getText(),
                    parseTotal(lblTotal.getText())
            );

            Stage stage = (Stage) lblTotal.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper parse tiền "150,000 VND" -> 150000
    private double parseTotal(String totalText) {
        return Double.parseDouble(totalText.replaceAll("[^0-9]", ""));
    }

    @FXML
    private void handleBack() {
        System.out.println("Quay lại Seat Screen");
    }

    // Setter để nhận dữ liệu từ SeatController (quan trọng)
    public void setBookingData(int movieId, int showtimeId, String seats, int price) {
        this.movieId = movieId;
        this.showtimeId = showtimeId;
        this.selectedSeats = seats;
        this.ticketPrice = price;
        loadMovieAndShowtime();
        calculatePrice();
    }
}