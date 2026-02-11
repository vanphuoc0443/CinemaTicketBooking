package app;

import fxcontroller.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        SceneManager.setStage(stage);

        // Start with login screen
        Parent root = FXMLLoader.load(getClass().getResource("/ui/view/login.fxml"));
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        stage.setTitle("🎬 Cinema Ticket Booking");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
