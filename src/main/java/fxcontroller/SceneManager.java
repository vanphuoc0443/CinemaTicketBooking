package fxcontroller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class SceneManager {

    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getStage() {
        return primaryStage;
    }

    /**
     * Switch scene with a smooth fade transition.
     * Preserves window size and maximized state.
     */
    public static void switchScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/ui/view/" + fxml));
            Parent root = loader.load();

            Scene currentScene = primaryStage.getScene();
            double width = currentScene != null ? currentScene.getWidth() : 1000;
            double height = currentScene != null ? currentScene.getHeight() : 700;

            // Fade out current content, then swap
            if (currentScene != null && currentScene.getRoot() != null) {
                Parent oldRoot = currentScene.getRoot();
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), oldRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    applyNewScene(root, width, height);
                    // Fade in new content
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();
            } else {
                applyNewScene(root, width, height);
            }

        } catch (IOException e) {
            System.err.println("❌ Cannot load FXML: " + fxml);
            e.printStackTrace();
        }
    }

    private static void applyNewScene(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(
                SceneManager.class.getResource("/css/style.css").toExternalForm());

        boolean wasMaximized = primaryStage.isMaximized();
        primaryStage.setScene(scene);
        if (wasMaximized) {
            primaryStage.setMaximized(true);
        }
    }
}
