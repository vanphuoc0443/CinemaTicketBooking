package fxcontroller;

import fxcontroller.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.setStage(stage);
        SceneManager.switchScene("login.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}
