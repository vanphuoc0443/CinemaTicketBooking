package fxcontroller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import java.util.HashSet;
import java.util.Set;

public class SeatController {

    // Nhận các nút từ FXML
    @FXML private Button A1, A2, A5, A6;
    @FXML private Button B1, B6;
    @FXML private Button C1, C2, C3, C6;

    private Set<String> selectedSeats = new HashSet<>();

    private final String MAU_TRONG = "#4caf50";   // ghế trống
    private final String MAU_CHON = "#2196f3";    // ghế đã chọn

    @FXML
    public void initialize() {
        // Set ghế trống = màu xanh
        A1.setStyle("-fx-background-color:" + MAU_TRONG);
        A2.setStyle("-fx-background-color:" + MAU_TRONG);
        A5.setStyle("-fx-background-color:" + MAU_TRONG);
        A6.setStyle("-fx-background-color:" + MAU_TRONG);

        B1.setStyle("-fx-background-color:" + MAU_TRONG);
        B6.setStyle("-fx-background-color:" + MAU_TRONG);

        C1.setStyle("-fx-background-color:" + MAU_TRONG);
        C2.setStyle("-fx-background-color:" + MAU_TRONG);
        C3.setStyle("-fx-background-color:" + MAU_TRONG);
        C6.setStyle("-fx-background-color:" + MAU_TRONG);
    }

    @FXML
    public void selectSeat(ActionEvent event) {
        Button seat = (Button) event.getSource();
        String id = seat.getId(); // A1, B6...

        if (selectedSeats.contains(id)) {
            selectedSeats.remove(id);
            seat.setStyle("-fx-background-color:#4caf50");
        } else {
            selectedSeats.add(id);
            seat.setStyle("-fx-background-color:#2196f3");
        }

        System.out.println("Ghế đã chọn: " + selectedSeats);
    }


    @FXML
    public void continueBooking() {
        if (selectedSeats.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Bạn chưa chọn ghế!");
            alert.show();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Ghế bạn chọn");
        alert.setContentText(selectedSeats.toString());
        alert.show();
    }
}
