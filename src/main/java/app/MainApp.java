package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/main-view.fxml")
        );

        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );

        stage.setTitle("Cinema Booking System");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
