package fxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SummaryController {

    @FXML private Label seatLabel;
    @FXML private Label priceLabel;

    public void initialize() {
        int total = SeatController.selectedSeats.size() * 5;
        seatLabel.setText("Seats: " + SeatController.selectedSeats);
        priceLabel.setText("Total: $" + total);
    }

    @FXML
    public void goPayment() {
        SceneManager.switchScene("payment.fxml");
    }
}
