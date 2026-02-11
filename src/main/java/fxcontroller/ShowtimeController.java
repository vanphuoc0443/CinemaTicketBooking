package fxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class ShowtimeController {

    @FXML
    private ListView<String> showtimeList;

    public void initialize() {
        showtimeList.getItems().addAll("10:00", "13:00", "16:00", "19:00");
    }

    @FXML
    public void goSeat() {
        System.out.println("Go to seat selection");
    }
}
