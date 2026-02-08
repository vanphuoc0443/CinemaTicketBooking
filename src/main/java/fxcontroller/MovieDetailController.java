package fxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MovieDetailController {

    @FXML private Label movieTitle;

    @FXML
    public void bookTicket() {
        System.out.println("Go to showtime");
    }
}
