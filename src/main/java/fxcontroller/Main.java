package fxcontroller;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.setStage(stage);
        stage.setTitle("🎬 Đặt Vé Xem Phim");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setMaximized(true);
        SceneManager.switchScene("login.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}
