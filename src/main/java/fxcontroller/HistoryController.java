package fxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import java.util.ArrayList;
import java.util.List;

public class HistoryController {

    public static List<String> history = new ArrayList<>();

    @FXML private ListView<String> historyList;

    public void initialize() {
        historyList.getItems().addAll(history);
    }

    @FXML
    public void home() {
        SceneManager.switchScene("home.fxml");
    }
}
