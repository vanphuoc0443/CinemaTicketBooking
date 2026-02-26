package fxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import util.DatabaseConnection;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;
    @FXML private Hyperlink btnGoRegister;

    @FXML
    public void initialize() {
        btnLogin.setOnAction(this::handleLogin);
        btnGoRegister.setOnAction(e -> openRegister());
    }

    private void handleLogin(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            lblError.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                openHome();
            } else {
                lblError.setText("Sai email hoặc mật khẩu!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Lỗi kết nối database!");
        }
    }

    private void openHome() {
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(
                    getClass().getResource("/ui/view/home.fxml")));
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openRegister() {
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(
                    getClass().getResource("/ui/view/register.fxml")));
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}