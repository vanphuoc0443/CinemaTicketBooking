package fxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TicketController {

    @FXML private Label ticketLabel;

    public void initialize() {
        ticketLabel.setText("🎟 Ticket\nSeats: " + SeatController.selectedSeats);
    }

    @FXML
    public void goHistory() {
        SceneManager.switchScene("history.fxml");
    }
}
