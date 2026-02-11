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
            lblMessage.setText("Please enter email and password");
            return;
        }

        try {
            CustomerDAO customerDAO = new CustomerDAO();
            Customer customer = customerDAO.login(email, password);

            if (customer != null) {
                Session.setCurrentCustomer(customer);
                switchScene("/ui/view/main.fxml");
            } else {
                lblMessage.setText("Invalid email or password");
            }

        } catch (Exception e) {
            lblMessage.setText("Database error");
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

    private void switchScene(String fxml) throws Exception {
        Stage stage = (Stage) txtEmail.getScene().getWindow();
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource(fxml)));
        stage.setScene(scene);
    }
}
