package fxcontroller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.Event;

public class HomeController {

    // Các poster phim (khớp với fx:id trong home.fxml)
    @FXML private ImageView movie1;
    @FXML private ImageView movie2;
    @FXML private ImageView movie3;
    @FXML private ImageView movie4;

    // Khi click vào 1 phim → mở màn hình chọn ghế
    @FXML
    public void openSeatScreen(Event event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/seat.fxml"));
            Scene scene = new Scene(root, 360, 800);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
