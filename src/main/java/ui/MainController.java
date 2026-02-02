package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

public class MainController {

    @FXML private ListView<String> movieList;
    @FXML private GridPane seatGrid;
    @FXML private Label totalLabel;

    private int total = 0;

    @FXML
    public void initialize() {
        loadMovies();
        buildSeats();
    }

    private void loadMovies() {
        movieList.getItems().addAll(
                "Avatar 2",
                "Dune Part 2",
                "Godzilla x Kong"
        );
    }

    private void buildSeats() {
        int seatNumber = 1;

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 6; col++) {
                String seatId = "A" + seatNumber++;

                Button seat = new Button(seatId);
                seat.getStyleClass().add("seat");

                seat.setOnAction(e -> {
                    if (seat.getStyleClass().contains("selected")) {
                        seat.getStyleClass().remove("selected");
                        total -= 50000;
                    } else {
                        seat.getStyleClass().add("selected");
                        total += 50000;
                    }
                    totalLabel.setText("Total: " + total + " VND");
                });

                seatGrid.add(seat, col, row);
            }
        }
    }
}
