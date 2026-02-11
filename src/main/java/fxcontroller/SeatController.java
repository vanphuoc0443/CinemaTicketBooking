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

    // Hardcoded buttons for the old static interface
    @FXML
    private Button A1, A2, A5, A6;
    @FXML
    private Button B1, B6;
    @FXML
    private Button C1, C2, C3, C6;

    // Shared state for other controllers (Changed to public static as
    // requested/needed)
    public static Set<String> selectedSeats = new HashSet<>();
    public static int totalPrice = 0;

    private final int PRICE_PER_SEAT = 50000;
    private final String MAU_TRONG = "#4caf50"; // Green
    private final String MAU_CHON = "#2196f3"; // Blue (or #fdd835 yellow in dynamic? sticking to old code style if
                                               // possible)
    // Old code (Step 1118) used #4caf50 and #2196f3.

    @FXML
    public void initialize() {
        // Initialize hardcoded buttons if they exist
        if (A1 != null) {
            initSeat(A1);
            initSeat(A2);
            initSeat(A5);
            initSeat(A6);
            initSeat(B1);
            initSeat(B6);
            initSeat(C1);
            initSeat(C2);
            initSeat(C3);
            initSeat(C6);
        }
        updateInfo();
    }

    private void initSeat(Button b) {
        b.setStyle("-fx-background-color:" + MAU_TRONG);
    }

    @FXML
    public void selectSeat(ActionEvent event) {
        Button seat = (Button) event.getSource();
        String id = seat.getId();

        if (selectedSeats.contains(id)) {
            selectedSeats.remove(id);
            seat.setStyle("-fx-background-color:" + MAU_TRONG);
        } else {
            selectedSeats.add(id);
            seat.setStyle("-fx-background-color:" + MAU_CHON);
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
    public void goSummary(ActionEvent event) { // Renamed continueBooking to match FXML if needed, or keep
                                               // continueBooking?
        // Step 1118 had continueBooking() method.
        // But seat.fxml (Step 934 old version) had onAction="#continueBooking".
        // Step 934 also had hardcoded A1, A2...
        // So I should keep continueBooking.

        continueBookingImpl(event);
    }

    @FXML
    public void continueBooking(ActionEvent event) { // supporting both names just in case
        continueBookingImpl(event);
    }

    @FXML
    public void continueBooking() { // supporting no-arg if used
        continueBookingImpl(null);
    }

    private void continueBookingImpl(ActionEvent event) {
        if (selectedSeats.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Bạn chưa chọn ghế!");
            alert.show();
            return;
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ui/view/summary.fxml"));
            // If event is null, we can't get stage easily. But usually triggered by button.
            // If triggered by button, event is not null.
            if (event != null) {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                // Fallback or old logic?
                // Old code (Step 1118) used continueBooking() void NO ARGS.
                // And it showed Alert with content. It DID NOT navigate?
                // Wait. Step 1118 lines 101-106: It navigates using event!!!!!
                // But method signature was `public void continueBooking()`.
                // How did it get `event`?
                // `Stage stage = (Stage) ((Node) event.getSource())...`
                // usage of `event` implies it was passed in?
                // But signature `public void continueBooking()` has NO args.
                // Java Compile Error: `event` cannot be resolved.
                // So the code in Step 1118 WAS BROKEN?
                // Or I misread it.
                // Step 1118 Line 58: `public void continueBooking() {`
                // Line 104: `((Node) event.getSource())`
                // YES, it used `event` but `event` was NOT defined in method.
                // This `SeatController` (Step 1118) was broken code!
                // That explains why build failed?
                // Build failure (Step 1093) was `selectedSeats private access`.
                // It didn't mention `event`. Maybe it didn't reach that line or `event` was a
                // field? (No field named event).

                // I MUST fix this.
                // I will create `public void continueBooking(ActionEvent event)`.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
