package fxcontroller;

import dao.CustomerDAO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import model.Customer;
import service.EmailService;

public class ForgotPasswordController {

    // Step 1: Email input
    @FXML
    private VBox emailSection;
    @FXML
    private TextField txtEmail;
    @FXML
    private Button btnSend;
    @FXML
    private ProgressIndicator loadingIndicator;

    // Step 2: Verification + new password
    @FXML
    private VBox verifySection;
    @FXML
    private TextField txtCode;
    @FXML
    private PasswordField txtNewPassword;
    @FXML
    private PasswordField txtConfirmPassword;

    // Shared
    @FXML
    private Label lblError;
    @FXML
    private Label lblSuccess;

    private final EmailService emailService = new EmailService();
    private String pendingEmail;

    @FXML
    public void handleSend(ActionEvent event) {
        String email = txtEmail.getText().trim();
        lblError.setText("");
        lblSuccess.setText("");

        if (email.isEmpty()) {
            lblError.setText("Vui lòng nhập email");
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            lblError.setText("Email không hợp lệ");
            return;
        }

        try {
            CustomerDAO dao = new CustomerDAO();
            Customer customer = dao.findByEmail(email);
            if (customer == null) {
                lblError.setText("Email chưa được đăng ký trong hệ thống");
                return;
            }
        } catch (Exception e) {
            lblError.setText("Lỗi kiểm tra tài khoản");
            e.printStackTrace();
            return;
        }

        pendingEmail = email;
        btnSend.setDisable(true);
        btnSend.setText("Đang gửi...");
        if (loadingIndicator != null)
            loadingIndicator.setVisible(true);

        Task<Boolean> sendTask = new Task<>() {
            @Override
            protected Boolean call() {
                return emailService.sendPasswordResetEmail(pendingEmail);
            }
        };

        sendTask.setOnSucceeded(e -> Platform.runLater(() -> {
            if (loadingIndicator != null)
                loadingIndicator.setVisible(false);
            btnSend.setDisable(false);
            btnSend.setText("📧 Gửi email đặt lại mật khẩu");

            if (sendTask.getValue()) {
                // Switch to verification step
                emailSection.setVisible(false);
                emailSection.setManaged(false);
                verifySection.setVisible(true);
                verifySection.setManaged(true);

                String maskedEmail = maskEmail(pendingEmail);
                lblSuccess.setText("✅ Đã gửi mã xác nhận đến " + maskedEmail);
                lblError.setText("");
            } else {
                String detail = EmailService.getLastError();
                lblError.setText("❌ Không thể gửi email: " + (detail != null ? detail : "Unknown error"));
            }
        }));

        sendTask.setOnFailed(e -> Platform.runLater(() -> {
            if (loadingIndicator != null)
                loadingIndicator.setVisible(false);
            btnSend.setDisable(false);
            btnSend.setText("📧 Gửi email đặt lại mật khẩu");
            lblError.setText("❌ Lỗi: " + sendTask.getException().getMessage());
        }));

        Thread thread = new Thread(sendTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void handleVerify(ActionEvent event) {
        String code = txtCode.getText().trim();
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmPassword.getText();
        lblError.setText("");
        lblSuccess.setText("");

        if (code.isEmpty()) {
            lblError.setText("Vui lòng nhập mã xác nhận");
            return;
        }

        String expectedToken = EmailService.getLastResetToken();
        if (expectedToken == null || !expectedToken.equalsIgnoreCase(code)) {
            lblError.setText("Mã xác nhận không đúng");
            return;
        }

        if (newPass.isEmpty() || newPass.length() < 6) {
            lblError.setText("Mật khẩu mới phải có ít nhất 6 ký tự");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            lblError.setText("Mật khẩu xác nhận không khớp");
            return;
        }

        try {
            CustomerDAO dao = new CustomerDAO();
            boolean updated = dao.updatePassword(pendingEmail, newPass);

            if (updated) {
                lblSuccess.setText("✅ Đổi mật khẩu thành công! Đang chuyển về đăng nhập...");
                lblError.setText("");

                // Navigate to login after 2 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                    Platform.runLater(() -> SceneManager.switchScene("login.fxml"));
                }).start();
            } else {
                lblError.setText("Không thể cập nhật mật khẩu");
            }
        } catch (Exception e) {
            lblError.setText("Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneManager.switchScene("login.fxml");
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2)
            return email;
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
