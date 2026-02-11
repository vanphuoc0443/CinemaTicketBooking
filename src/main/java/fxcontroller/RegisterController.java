package fxcontroller;

import dao.CustomerDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Customer;

public class RegisterController {

    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirm;
    @FXML private Label lblMessage;

    @FXML
    public void handleRegister() {

        String username = txtUsername.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String confirm = txtConfirm.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Please fill all fields");
            return;
        }

        if (!password.equals(confirm)) {
            lblMessage.setText("Passwords do not match");
            return;
        }

        try {
            CustomerDAO dao = new CustomerDAO();

            // Check email exists
            if (dao.emailExists(email)) {
                lblMessage.setText("Email already exists");
                return;
            }

            Customer customer = new Customer();
            customer.setName(username);
            customer.setEmail(email);

            boolean success = dao.register(customer, password);

            if (success) {
                Stage stage = (Stage) txtUsername.getScene().getWindow();
                stage.setScene(new Scene(
                        FXMLLoader.load(getClass().getResource("/ui/view/login.fxml"))

                ));
            } else {
                lblMessage.setText("Register failed");
            }

        } catch (Exception e) {
            lblMessage.setText("Database error");
            e.printStackTrace();
        }
    }

    @FXML
    public void backToLogin() {
        try {
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(
                    FXMLLoader.load(getClass().getResource("/ui/view/login.fxml"))

            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
