package fxcontroller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class SeatController implements Initializable {

    @FXML
    private GridPane seatGrid;

    @FXML
    private Label selectedSeatLabel;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Label movieTitleLabel;

    @FXML
    private Label showtimeLabel;

    // CẤU HÌNH RẠP (giống app cinema thật)
    private final int ROWS = 6;
    private final int COLS = 8;
    private final int SEAT_PRICE = 75000;

    // Dữ liệu ghế
    private final Set<String> selectedSeats = new HashSet<>();
    private final Set<String> bookedSeats = new HashSet<>();

    // Dữ liệu nhận từ màn trước (Showtime/Home)
    private String movieTitle = "Unknown Movie";
    private String showtime = "Unknown Showtime";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadBookedSeatsFromDB();
        generateSeatMap();
        updateSelectionInfo();
    }

    /**
     * METHOD QUAN TRỌNG:
     * Dùng để nhận dữ liệu từ ShowtimeController/HomeController
     */
    public void setShowtimeData(String movieTitle, String showtime) {
        this.movieTitle = movieTitle;
        this.showtime = showtime;

        movieTitleLabel.setText(movieTitle);
        showtimeLabel.setText(showtime);
    }

    // Giả lập lấy ghế đã đặt từ database
    private void loadBookedSeatsFromDB() {
        // TODO: Thay bằng query DB thật (theo showtime_id)
        bookedSeats.add("A3");
        bookedSeats.add("B5");
        bookedSeats.add("C2");
        bookedSeats.add("D7");
    }

    private void generateSeatMap() {
        seatGrid.getChildren().clear();

        for (int row = 0; row < ROWS; row++) {
            char rowChar = (char) ('A' + row);

            for (int col = 1; col <= COLS; col++) {
                String seatId = rowChar + String.valueOf(col);

                Button seatBtn = new Button(seatId);
                seatBtn.setPrefSize(40, 40);
                seatBtn.setStyle(getSeatStyle(seatId));

                if (bookedSeats.contains(seatId)) {
                    seatBtn.setDisable(true);
                } else {
                    seatBtn.setOnAction(e -> handleSeatSelection(seatBtn, seatId));
                }

                seatGrid.add(seatBtn, col - 1, row);
            }
        }
    }

    private void handleSeatSelection(Button seatBtn, String seatId) {
        if (selectedSeats.contains(seatId)) {
            selectedSeats.remove(seatId);
            seatBtn.setStyle(getAvailableSeatStyle());
        } else {
            selectedSeats.add(seatId);
            seatBtn.setStyle(getSelectedSeatStyle());
        }
        updateSelectionInfo();
    }

    private void updateSelectionInfo() {
        if (selectedSeats.isEmpty()) {
            selectedSeatLabel.setText("Ghế đã chọn: Chưa chọn");
        } else {
            List<String> sortedSeats = new ArrayList<>(selectedSeats);
            Collections.sort(sortedSeats);
            selectedSeatLabel.setText("Ghế đã chọn: " + String.join(", ", sortedSeats));
        }

        int total = selectedSeats.size() * SEAT_PRICE;
        totalPriceLabel.setText("Tổng tiền: " + String.format("%,d", total) + " VND");
    }

    private String getSeatStyle(String seatId) {
        if (bookedSeats.contains(seatId)) {
            // Ghế đã đặt (đỏ)
            return "-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-background-radius: 8;";
        }
        return getAvailableSeatStyle();
    }

    private String getAvailableSeatStyle() {
        // Ghế trống (xám)
        return "-fx-background-color: #2b2b2b; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;";
    }

    private String getSelectedSeatStyle() {
        // Ghế đang chọn (vàng - giống CGV style)
        return "-fx-background-color: #ffd369; -fx-text-fill: black; -fx-background-radius: 8;";
    }

    @FXML
    private void handleContinuePayment() {
        if (selectedSeats.isEmpty()) {
            selectedSeatLabel.setText("Vui lòng chọn ít nhất 1 ghế!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/view/payment.fxml")
            );
            Parent root = loader.load();

            // Lấy controller
            PaymentController paymentController = loader.getController();

            // Convert ghế thành chuỗi: A1, A2, B3
            List<String> sortedSeats = new ArrayList<>(selectedSeats);
            Collections.sort(sortedSeats);
            String seatsString = String.join(", ", sortedSeats);

            // Truyền đúng format KHỚP với PaymentController (rất quan trọng)
            paymentController.setPaymentData(
                    movieTitle,                 // tên phim
                    showtime,                   // giờ chiếu
                    "Phòng 3",                  // room (có thể truyền động sau)
                    seatsString,                // ghế dạng String
                    "Không",                   // combo (tạm)
                    selectedSeats.size() * SEAT_PRICE  // tổng tiền
            );

            Stage stage = (Stage) seatGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}