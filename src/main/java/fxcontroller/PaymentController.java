package fxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PaymentController {

    @FXML private Button btnBack;
    @FXML private Button btnMomo;
    @FXML private Button btnZaloPay;
    @FXML private Button btnCard;
    @FXML private Button btnConfirm;

    @FXML private Label lblMovieName;
    @FXML private Label lblShowtime;
    @FXML private Label lblRoom;
    @FXML private Label lblSeats;
    @FXML private Label lblCombo;
    @FXML private Label lblTotal;
    @FXML private Label lblStatus;

    private String selectedMethod = "";
    private int bookingId; // ❗ BỎ hardcode = 1

    // ====== NHẬN DATA TỪ MÀN TRƯỚC (Seat / Summary) ======
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
        loadBookingFromDB(); // Load đúng vé vừa đặt
    }

    // (OPTIONAL) Nếu bạn chưa lưu DB, có thể truyền tạm data
    public void setPaymentData(String movie, String showtime, String room,
                               String seats, String combo, double total) {
        lblMovieName.setText(movie);
        lblShowtime.setText("🕒 " + showtime);
        lblRoom.setText("📍 Phòng: " + room);
        lblSeats.setText("Ghế: " + seats);
        lblCombo.setText("Combo: " + combo);
        lblTotal.setText(total + " đ");
    }

    @FXML
    public void initialize() {
        setupEvents();
    }

    // 🎬 LOAD DATA FROM DATABASE (CHỈ LOAD KHI CÓ bookingId)
    private void loadBookingFromDB() {
        if (bookingId <= 0) {
            lblStatus.setText("Chưa có dữ liệu đặt vé!");
            return;
        }

        String sql = """
            SELECT m.title, s.show_time, s.room, 
                   b.seats, b.combo, b.total_price
            FROM bookings b
            JOIN showtimes s ON b.showtime_id = s.showtime_id
            JOIN movies m ON s.movie_id = m.movie_id
            WHERE b.booking_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                lblMovieName.setText(rs.getString("title"));
                lblShowtime.setText("🕒 " + rs.getString("show_time"));
                lblRoom.setText("📍 Phòng: " + rs.getString("room"));
                lblSeats.setText("Ghế: " + rs.getString("seats"));
                lblCombo.setText("Combo: " + rs.getString("combo"));
                lblTotal.setText(rs.getDouble("total_price") + " đ");
            } else {
                lblStatus.setText("Không tìm thấy dữ liệu đặt vé!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Lỗi kết nối database!");
        }
    }

    // 🎮 EVENTS GIỐNG APP CGV / CINEMA
    private void setupEvents() {

        btnBack.setOnAction(e -> goBack());

        btnMomo.setOnAction(e -> selectMethod("MoMo"));
        btnZaloPay.setOnAction(e -> selectMethod("ZaloPay"));
        btnCard.setOnAction(e -> selectMethod("Credit/Debit Card"));

        btnConfirm.setOnAction(e -> confirmPayment());
    }

    private void selectMethod(String method) {
        selectedMethod = method;

        // Highlight button giống app thật
        btnMomo.setStyle("-fx-background-color: #2a2a2a;");
        btnZaloPay.setStyle("-fx-background-color: #2a2a2a;");
        btnCard.setStyle("-fx-background-color: #2a2a2a;");

        switch (method) {
            case "MoMo" -> btnMomo.setStyle("-fx-background-color: #d82d8b; -fx-text-fill: white;");
            case "ZaloPay" -> btnZaloPay.setStyle("-fx-background-color: #0068ff; -fx-text-fill: white;");
            case "Credit/Debit Card" -> btnCard.setStyle("-fx-background-color: #ffd369; -fx-text-fill: black;");
        }

        lblStatus.setText("Đã chọn: " + method);
    }

    private void confirmPayment() {
        if (selectedMethod.isEmpty()) {
            lblStatus.setText("⚠ Vui lòng chọn phương thức thanh toán!");
            return;
        }

        if (bookingId <= 0) {
            lblStatus.setText("Không có booking để thanh toán!");
            return;
        }

        String sql = """
            UPDATE bookings 
            SET payment_method = ?, status = 'PAID' 
            WHERE booking_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, selectedMethod);
            ps.setInt(2, bookingId);
            ps.executeUpdate();

            lblStatus.setText("🎉 Thanh toán thành công!");
            openTicket();

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Thanh toán thất bại!");
        }
    }

    private void goBack() {
        try {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(
                    getClass().getResource("/ui/view/summary.fxml")));
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openTicket() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/view/ticket.fxml")
            );

            Scene scene = new Scene(loader.load());

            // Truyền bookingId sang Ticket (chuẩn app thật)
            TicketController controller = loader.getController();
            controller.setBookingId(bookingId);

            Stage stage = (Stage) btnConfirm.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}