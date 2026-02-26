package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Ứng dụng quản lý dành cho Admin — hoàn toàn tách biệt khỏi phần mềm người
 * dùng.
 * Chạy bằng: mvn javafx:run -Djavafx.mainClass=app.AdminApp
 * Hoặc thêm exec profile riêng.
 */
public class AdminApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ui/view/admin.fxml"));
        Scene scene = new Scene(root, 1100, 750);
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        stage.setTitle("⚙ CINEMA ADMIN — Quản lý phòng chiếu & suất chiếu");
        stage.setScene(scene);
        stage.setMinWidth(950);
        stage.setMinHeight(650);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
