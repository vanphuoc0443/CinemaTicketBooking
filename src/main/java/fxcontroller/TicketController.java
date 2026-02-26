package fxcontroller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;


public class TicketController {

    @FXML private Button btnBack;

    @FXML private ImageView imgPoster;
    @FXML private ImageView imgQR;

    @FXML private Label lblMovieTitle;
    @FXML private Label lblGenre;
    @FXML private Label lblDuration;
    @FXML private Label lblRoom;
    @FXML private Label lblShowtime;
    @FXML private Label lblSeats;
    @FXML private Label lblTotal;
    @FXML private Label lblStatus;

    // Sau này truyền từ PaymentController
    private int bookingId = 1;

    @FXML
    public void initialize() {
        loadTicketFromDB();
        setupEvents();
        generateQRCode(bookingId); // chuẩn flow cinema app
    }

    // 🎟 LOAD VÉ TỪ DATABASE (CHUẨN FLOW APP RẠP)
    private void loadTicketFromDB() {
        String sql = """
            SELECT m.title, m.genre, m.duration, m.poster_url,
                   s.show_time, s.room,
                   b.seats, b.total_price, b.status
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
                lblMovieTitle.setText(rs.getString("title"));
                lblGenre.setText(rs.getString("genre"));
                lblDuration.setText("⏱ " + rs.getInt("duration") + " phút");
                lblRoom.setText("📍 Phòng " + rs.getString("room"));
                lblShowtime.setText(rs.getString("show_time"));
                lblSeats.setText(rs.getString("seats"));
                lblTotal.setText("Tổng: " + rs.getDouble("total_price") + " đ");

                String status = rs.getString("status");
                lblStatus.setText("PAID".equals(status) ? "🎉 Vé đã thanh toán" : "Chưa thanh toán");

                // Load Poster
                String posterUrl = rs.getString("poster_url");
                if (posterUrl != null && !posterUrl.isEmpty()) {
                    imgPoster.setImage(new Image(posterUrl, true));
                }

            } else {
                lblStatus.setText("Không tìm thấy vé!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Lỗi tải dữ liệu vé!");
        }
    }

    // 🎮 EVENTS
    private void setupEvents() {
        btnBack.setOnAction(e -> goToHistory());
    }

    // 🔙 Quay về History (flow chuẩn sau khi thanh toán)
    private void goToHistory() {
        try {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(
                    getClass().getResource("/ui/view/history.fxml")));
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateQRCode(int bookingId) {
        try {
            String qrData = "CINEMA_BOOKING_" + bookingId;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    qrData,
                    BarcodeFormat.QR_CODE,
                    200,
                    200
            );

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            WritableImage image = new WritableImage(width, height);
            PixelWriter pixelWriter = image.getPixelWriter();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pixelWriter.setColor(
                            x,
                            y,
                            bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE
                    );
                }
            }

            imgQR.setImage(image);

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Lỗi tạo QR Code!");
        }
    }

    // 🔗 Cho phép truyền bookingId từ PaymentController (PRO)
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
        loadTicketFromDB();
    }
}