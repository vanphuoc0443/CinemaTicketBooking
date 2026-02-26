package fxcontroller;

import dao.CustomerDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Customer;
import util.Session;

import java.text.SimpleDateFormat;

public class ProfileController {

    @FXML
    private Label lblAvatar;
    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblMemberSince;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtPhone;
    @FXML
    private Label lblError;
    @FXML
    private Label lblSuccess;
    @FXML
    private Button btnSave;

    private Customer customer;

    @FXML
    public void initialize() {
        customer = Session.getCurrentCustomer();
        if (customer == null) {
            SceneManager.switchScene("login.fxml");
            return;
        }

        // Populate fields
        txtName.setText(customer.getName() != null ? customer.getName() : "");
        txtEmail.setText(customer.getEmail() != null ? customer.getEmail() : "");
        txtPhone.setText(customer.getPhone() != null ? customer.getPhone() : "");

        // Avatar — first letter of name
        String initial = customer.getName() != null && !customer.getName().isEmpty()
                ? customer.getName().substring(0, 1).toUpperCase()
                : "?";
        lblAvatar.setText(initial);

        // Welcome
        lblWelcome.setText(customer.getName() != null ? customer.getName() : "Người dùng");

        // Member since
        if (customer.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            lblMemberSince.setText("Thành viên từ " + sdf.format(customer.getCreatedAt()));
        } else {
            lblMemberSince.setText("");
        }
    }

    @FXML
    public void handleSave() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        lblError.setText("");
        lblSuccess.setText("");

        if (name.isEmpty()) {
            lblError.setText("Tên không được để trống");
            return;
        }

        if (email.isEmpty() || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            lblError.setText("Email không hợp lệ");
            return;
        }

        if (!phone.isEmpty() && !phone.matches("^[0-9]{9,11}$")) {
            lblError.setText("Số điện thoại không hợp lệ (9-11 chữ số)");
            return;
        }

        try {
            CustomerDAO dao = new CustomerDAO();

            // Check if email changed and new email is already taken
            if (!email.equals(customer.getEmail()) && dao.emailExistsExcept(email, customer.getCustomerId())) {
                lblError.setText("Email này đã được sử dụng bởi tài khoản khác");
                return;
            }

            // Update customer object
            customer.setName(name);
            customer.setPhone(phone.isEmpty() ? null : phone);

            // Update basic info (name, phone)
            boolean updated = dao.update(customer);

            // If email changed, update email separately
            if (!email.equals(customer.getEmail())) {
                boolean emailUpdated = dao.updateEmail(customer.getCustomerId(), email);
                if (emailUpdated) {
                    customer.setEmail(email);
                }
            }

            if (updated) {
                // Refresh session
                Session.setCurrentCustomer(customer);

                // Update avatar
                String initial = name.substring(0, 1).toUpperCase();
                lblAvatar.setText(initial);
                lblWelcome.setText(name);

                lblSuccess.setText("✅ Cập nhật thông tin thành công!");
            } else {
                lblError.setText("Không thể cập nhật thông tin");
            }
        } catch (Exception e) {
            lblError.setText("Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack() {
        SceneManager.switchScene("home.fxml");
    }
}
