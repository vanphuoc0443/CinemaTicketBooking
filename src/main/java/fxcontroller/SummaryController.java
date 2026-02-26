package fxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.Movie;

public class SummaryController {

    @FXML
    private Label seatLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label movieLabel;
    @FXML
    private Label showtimeLabel;

    public void initialize() {
        // Movie info
        Movie movie = HomeController.selectedMovie;
        if (movie != null) {
            movieLabel.setText(movie.getTitle());
        } else {
            movieLabel.setText("Chưa chọn phim");
        }

        // Showtime info
        String date = MovieDetailController.selectedDate;
        String time = MovieDetailController.selectedShowtime;
        if (date != null && time != null) {
            showtimeLabel.setText(date + " - " + time);
        } else {
            showtimeLabel.setText("Chưa chọn suất");
        }

        // Seats in VND
        seatLabel.setText(String.join(", ", SeatController.selectedSeats));
        priceLabel.setText(String.format("%,dđ", SeatController.totalPrice).replace(',', '.'));
    }

    @FXML
    public void goPayment() {
        SceneManager.switchScene("payment.fxml");
    }
}
