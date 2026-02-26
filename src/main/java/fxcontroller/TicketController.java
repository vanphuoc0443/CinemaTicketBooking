package fxcontroller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import model.Movie;

public class TicketController {

    @FXML
    private Label ticketLabel;
    @FXML
    private Label movieTicketLabel;
    @FXML
    private Label seatsTicketLabel;
    @FXML
    private Label priceTicketLabel;
    @FXML
    private Label showtimeTicketLabel;
    @FXML
    private ImageView qrCodeImage;

    public void initialize() {
        Movie movie = HomeController.selectedMovie;

        String movieTitle = movie != null ? movie.getTitle() : "—";
        String seats = String.join(", ", SeatController.selectedSeats);
        String price = String.format("%,dđ", SeatController.totalPrice).replace(',', '.');

        String date = MovieDetailController.selectedDate != null ? MovieDetailController.selectedDate : "";
        String time = MovieDetailController.selectedShowtime != null ? MovieDetailController.selectedShowtime : "";
        String showtime = date + " - " + time;

        movieTicketLabel.setText(movieTitle);
        seatsTicketLabel.setText(seats);
        priceTicketLabel.setText(price);
        showtimeTicketLabel.setText(showtime);
        ticketLabel.setText("🎟 " + SeatController.selectedSeats.size() + " vé");

        // Generate QR code
        String qrContent = "CINEMA-TICKET\n"
                + "Phim: " + movieTitle + "\n"
                + "Suất: " + showtime + "\n"
                + "Ghế: " + seats + "\n"
                + "Tổng: " + price;

        if (qrCodeImage != null) {
            generateQRCode(qrContent, 180);
        }
    }

    private void generateQRCode(String content, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);

            WritableImage image = new WritableImage(size, size);
            PixelWriter pw = image.getPixelWriter();

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    pw.setColor(x, y, matrix.get(x, y)
                            ? Color.web("#1a1a2e") // QR dark pixels — dark blue
                            : Color.web("#eef0f4")); // QR light pixels — light gray
                }
            }

            qrCodeImage.setImage(image);
        } catch (WriterException e) {
            System.err.println("QR generation failed: " + e.getMessage());
        }
    }

    @FXML
    public void goHome() {
        SceneManager.switchScene("home.fxml");
    }

    @FXML
    public void goHistory() {
        SceneManager.switchScene("history.fxml");
    }
}
