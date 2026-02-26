package fxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ShowtimeController {

    @FXML
    private ImageView imgPoster;
    @FXML
    private Label lblMovieTitle;
    @FXML
    private Label lblGenre;
    @FXML
    private Label lblRelease;
    @FXML
    private VBox showtimeContainer;
    @FXML
    private HBox dateContainer;
    @FXML
    private Button btnContinue;

    private int selectedShowtimeId = -1;
    private int movieId = 1; // sẽ set từ HomeController

    @FXML
    public void initialize() {
        loadMovieInfo();
        loadDates();
        loadShowtimes();
    }

    private void loadMovieInfo() {
        String sql = "SELECT * FROM movies WHERE movie_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                lblMovieTitle.setText(rs.getString("title"));
                lblGenre.setText(rs.getString("genre") + " | " + rs.getInt("duration") + " phút");
                lblRelease.setText("Khởi chiếu: " + rs.getDate("release_date"));

                String poster = rs.getString("poster_url");
                imgPoster.setImage(new Image(poster, true));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDates() {
        String[] dates = {"Hôm nay", "Ngày mai", "T7", "CN"};

        for (String d : dates) {
            Button btnDate = new Button(d);
            btnDate.setStyle("""
                    -fx-background-color: #1E293B;
                    -fx-text-fill: white;
                    -fx-background-radius: 20;
                    -fx-padding: 8 16;
                    -fx-cursor: hand;
                    """);

            btnDate.setOnAction(e -> {
                highlightDate(btnDate);
                loadShowtimes();
            });

            dateContainer.getChildren().add(btnDate);
        }
    }

    private void highlightDate(Button selected) {
        for (var node : dateContainer.getChildren()) {
            node.setStyle("""
                    -fx-background-color: #1E293B;
                    -fx-text-fill: white;
                    -fx-background-radius: 20;
                    -fx-padding: 8 16;
                    """);
        }

        selected.setStyle("""
                -fx-background-color: #EF4444;
                -fx-text-fill: white;
                -fx-background-radius: 20;
                -fx-padding: 8 16;
                """);
    }

    private void loadShowtimes() {
        showtimeContainer.getChildren().clear();

        String sql = """
                SELECT showtime_id, show_time, room_name
                FROM showtimes
                WHERE movie_id = ?
                ORDER BY show_time
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int showtimeId = rs.getInt("showtime_id");
                String time = rs.getString("show_time");
                String room = rs.getString("room_name");

                HBox card = createShowtimeCard(showtimeId, time, room);
                showtimeContainer.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createShowtimeCard(int id, String time, String room) {
        HBox box = new HBox(15);
        box.setStyle("""
                -fx-background-color: #1E293B;
                -fx-background-radius: 18;
                -fx-padding: 15;
                -fx-cursor: hand;
                """);

        Label lblTime = new Label(time);
        lblTime.setStyle("""
                -fx-text-fill: #38BDF8;
                -fx-font-size: 20;
                -fx-font-weight: bold;
                """);

        Label lblRoom = new Label("Phòng: " + room);
        lblRoom.setStyle("-fx-text-fill: #CBD5F5; -fx-font-size: 14;");

        VBox info = new VBox(5, lblTime, lblRoom);
        box.getChildren().add(info);

        box.setOnMouseClicked(e -> {
            selectedShowtimeId = id;
            btnContinue.setDisable(false);
            highlightSelected(box);
        });

        return box;
    }

    private void highlightSelected(HBox selected) {
        for (var node : showtimeContainer.getChildren()) {
            node.setStyle("""
                    -fx-background-color: #1E293B;
                    -fx-background-radius: 18;
                    -fx-padding: 15;
                    """);
        }

        selected.setStyle("""
                -fx-background-color: linear-gradient(to right,#EF4444,#F59E0B);
                -fx-background-radius: 18;
                -fx-padding: 15;
                """);
    }

    @FXML
    private void handleContinue() {
        System.out.println("Selected showtime: " + selectedShowtimeId);
        // TODO: chuyển sang seat.fxml
    }

    @FXML
    private void handleBack() {
        System.out.println("Back to Home");
    }
}