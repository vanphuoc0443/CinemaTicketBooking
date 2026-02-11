package fxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class PaymentController {

    @FXML private ComboBox<String> methodBox;

    public void initialize() {
        methodBox.getItems().addAll("Credit Card", "Momo", "Paypal");
        methodBox.getSelectionModel().selectFirst();
    }

    @FXML
    public void pay() {
        HistoryController.history.add("Seats: " + SeatController.selectedSeats);
        SceneManager.switchScene("ticket.fxml");
    }
}
