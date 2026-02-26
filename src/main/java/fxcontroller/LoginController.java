package fxcontroller;

import dao.CustomerDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Customer;
import util.Session;

public class LoginController {

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMessage;

    @FXML
    public void handleLogin(ActionEvent event) {

        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Vui lòng nhập tên đăng nhập và mật khẩu");
            return;
        }

        try {
            CustomerDAO customerDAO = new CustomerDAO();
            Customer customer = customerDAO.login(email, password);

            if (customer != null) {
                Session.setCurrentCustomer(customer);
                switchScene("/ui/view/home.fxml");
            } else {
                lblMessage.setText("Tên đăng nhập hoặc mật khẩu không đúng");
            }

        } catch (Exception e) {
            lblMessage.setText("Lỗi kết nối cơ sở dữ liệu");
            e.printStackTrace();
        }
    }

    @FXML
    public void goToRegister(ActionEvent event) {
        try {
            switchScene("/ui/view/register.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goForgotPassword(ActionEvent event) {
        try {
            switchScene("/ui/view/forgot_password.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchScene(String fxml) throws Exception {
        Stage stage = (Stage) txtEmail.getScene().getWindow();
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource(fxml)));
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
    }
}
