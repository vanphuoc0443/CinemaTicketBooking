package fxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HistoryController {

    @FXML
    private VBox historyContainer;

    // Giả sử bạn đã login và có userId
    private int currentUserId = 1; // sau này lấy từ session/login

    @FXML
    public void initialize() {
        loadBookingHistory();
    }

    private void loadBookingHistory() {
        String sql = """
            SELECT b.booking_id, m.title, m.poster_url, 
                   s.seat_number, sh.show_time, c.name AS cinema_name
            FROM bookings b
            JOIN showtimes sh ON b.showtime_id = sh.showtime_id
            JOIN movies m ON sh.movie_id = m.movie_id
            JOIN seats s ON b.seat_id = s.seat_id
            JOIN cinemas c ON sh.cinema_id = c.cinema_id
            WHERE b.user_id = ?
            ORDER BY b.created_at DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();

            historyContainer.getChildren().clear();

            while (rs.next()) {
                HBox card = createHistoryCard(
                        rs.getString("title"),
                        rs.getString("poster_url"),
                        rs.getString("seat_number"),
                        rs.getString("cinema_name"),
                        rs.getString("show_time")
                );

                historyContainer.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createHistoryCard(String title, String posterUrl,
                                   String seat, String cinema, String time) {

        HBox card = new HBox();
        card.setSpacing(12);
        card.setStyle("""
            -fx-background-color: #1E1E2E;
            -fx-background-radius: 16;
            -fx-padding: 12;
        """);

        // Poster
        ImageView poster = new ImageView();
        try {
            poster.setImage(new Image(posterUrl, 60, 80, true, true));
        } catch (Exception e) {
            poster.setImage(new Image(
                    getClass().getResource("/images/default_poster.png").toExternalForm(),
                    60, 80, true, true
            ));
        }
        poster.setFitWidth(60);
        poster.setFitHeight(80);
        poster.setStyle("-fx-background-radius: 10;");

        // Info Box
        VBox infoBox = new VBox();
        infoBox.setSpacing(4);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 16px;
            -fx-font-weight: bold;
        """);

        Label cinemaLabel = new Label("Rạp: " + cinema);
        cinemaLabel.setStyle("-fx-text-fill: #BBBBBB; -fx-font-size: 12px;");

        Label seatLabel = new Label("Ghế: " + seat);
        seatLabel.setStyle("-fx-text-fill: #BBBBBB; -fx-font-size: 12px;");

        Label timeLabel = new Label("Suất chiếu: " + time);
        timeLabel.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 12px;");

        infoBox.getChildren().addAll(titleLabel, cinemaLabel, seatLabel, timeLabel);

        card.getChildren().addAll(poster, infoBox);

        return card;
    }
}