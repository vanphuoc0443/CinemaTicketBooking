package fxcontroller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    private static Stage primaryStage;

    // Gọi 1 lần trong Main.java
    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    // Chuyển màn hình
    public static void switchScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/ui/view/" + fxml)
            );
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("❌ Cannot load FXML: " + fxml);
            e.printStackTrace();
        }
    }
}
