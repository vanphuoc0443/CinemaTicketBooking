package fxcontroller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SeatController {

    @FXML
    private GridPane seatGrid;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Label selectedCountLabel;
    @FXML
    private Label screenLabel;
    @FXML
    private Button continueBtn;

    // Shared state for other controllers
    public static Set<String> selectedSeats = new HashSet<>();
    public static int totalPrice = 0;

    private final int ROWS = 5;
    private final int COLS = 8;
    private final int PRICE_PER_SEAT = 50000;

    @FXML
    public void initialize() {
        if (seatGrid != null) {
            loadSeats();
        }
        updateInfo();
    }

    private void loadSeats() {
        seatGrid.getChildren().clear();
        char[] rows = { 'A', 'B', 'C', 'D', 'E' };

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                String seatName = rows[i] + String.valueOf(j + 1);
                Button seat = new Button(seatName);
                seat.setPrefSize(40, 40);

                // Check if already selected (persistence)
                if (selectedSeats.contains(seatName)) {
                    seat.setStyle("-fx-background-color: #fdd835; -fx-text-fill: black;");
                } else {
                    seat.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
                }

                seat.setOnAction(e -> toggleSeat(seat, seatName));
                seatGrid.add(seat, j, i);
            }
        }
    }

    private void toggleSeat(Button seat, String seatName) {
        if (selectedSeats.contains(seatName)) {
            selectedSeats.remove(seatName);
            seat.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        } else {
            selectedSeats.add(seatName);
            seat.setStyle("-fx-background-color: #fdd835; -fx-text-fill: black;");
        }
        updateInfo();
    }

    private void updateInfo() {
        totalPrice = selectedSeats.size() * PRICE_PER_SEAT;
        if (selectedCountLabel != null) {
            selectedCountLabel.setText("Selected: " + selectedSeats.size());
        }
        if (totalPriceLabel != null) {
            totalPriceLabel.setText(String.format("%,d VNĐ", totalPrice));
        }
    }

    @FXML
    public void goSummary(ActionEvent event) {
        if (selectedSeats.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Vui lòng chọn ít nhất một ghế!"); // Vietnamese comment/text
            alert.show();
            return;
        }

        try {
            // Navigate to Summary
            Parent root = FXMLLoader.load(getClass().getResource("/ui/view/summary.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
