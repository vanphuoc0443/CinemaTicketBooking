package fxcontroller;

import dao.CustomerDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Customer;

public class RegisterController {

    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtConfirm;
    @FXML
    private Label lblMessage;

    @FXML
    public void handleRegister() {
        String username = txtUsername.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String confirm = txtConfirm.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Vui lòng điền đầy đủ thông tin");
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            lblMessage.setText("Email không hợp lệ");
            return;
        }

        if (password.length() < 6) {
            lblMessage.setText("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            lblMessage.setText("Mật khẩu phải có ít nhất 1 chữ hoa");
            return;
        }

        if (!password.matches(".*[0-9].*")) {
            lblMessage.setText("Mật khẩu phải có ít nhất 1 chữ số");
            return;
        }

        if (!password.equals(confirm)) {
            lblMessage.setText("Mật khẩu xác nhận không khớp");
            return;
        }

        try {
            CustomerDAO dao = new CustomerDAO();

            if (dao.emailExists(email)) {
                lblMessage.setText("Email đã được đăng ký");
                return;
            }

            Customer customer = new Customer();
            customer.setName(username);
            customer.setEmail(email);

            boolean success = dao.register(customer, password);

            if (success) {
                SceneManager.switchScene("login.fxml");
            } else {
                lblMessage.setText("Đăng ký thất bại");
            }

        } catch (Exception e) {
            lblMessage.setText("Lỗi cơ sở dữ liệu");
            e.printStackTrace();
        }
    }

    @FXML
    public void backToLogin() {
        SceneManager.switchScene("login.fxml");
    }
}
