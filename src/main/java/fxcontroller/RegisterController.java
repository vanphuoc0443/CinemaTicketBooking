package fxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import util.DatabaseConnection;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblMessage;
    @FXML private Button btnRegister;
    @FXML private Hyperlink btnGoLogin;

    @FXML
    public void initialize() {
        btnRegister.setOnAction(e -> handleRegister());
        btnGoLogin.setOnAction(e -> openLogin());
    }

    private void handleRegister() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();
        String confirm = txtConfirmPassword.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!password.equals(confirm)) {
            lblMessage.setText("Mật khẩu xác nhận không khớp!");
            return;
        }

        String sql = "INSERT INTO users(name, email, password) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);

            ps.executeUpdate();
            lblMessage.setStyle("-fx-text-fill: #22C55E;");
            lblMessage.setText("Đăng ký thành công!");
            openLogin();

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Email đã tồn tại hoặc lỗi DB!");
        }
    }

    private void openLogin() {
        try {
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(
                    getClass().getResource("/ui/view/login.fxml")));
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}