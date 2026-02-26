package fxcontroller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MovieDetailController {

    @FXML private Button btnBack;
    @FXML private Button btnBook;

    @FXML private ImageView imgPoster;
    @FXML private Label lblTitle;
    @FXML private Label lblGenre;
    @FXML private Label lblDuration;
    @FXML private Label lblRating;
    @FXML private Label lblDescription;
    @FXML private Label lblDirector;
    @FXML private Label lblReleaseDate;

    private int movieId; // Sau này truyền từ HomeController

    @FXML
    public void initialize() {
        setupEvents();
    }

    // 🎬 LOAD MOVIE FROM DATABASE (REAL APP STYLE)
    private void loadMovieFromDB() {
        String sql = "SELECT * FROM movies WHERE movie_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                lblTitle.setText(rs.getString("title"));
                lblGenre.setText(rs.getString("genre"));
                lblDuration.setText(rs.getInt("duration") + " phút");
                lblDescription.setText(rs.getString("description"));
                lblDirector.setText(rs.getString("director"));
                lblReleaseDate.setText(rs.getString("release_date"));

                // Load poster
                String posterUrl = rs.getString("poster_url");
                if (posterUrl != null && !posterUrl.isEmpty()) {
                    imgPoster.setImage(new Image(posterUrl));
                }

                // Fake rating (nếu DB chưa có)
                lblRating.setText("⭐ 8.5/10");
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblTitle.setText("Không thể tải dữ liệu phim");
        }
    }

    private void setupEvents() {
        btnBack.setOnAction(e -> goBackHome());
        btnBook.setOnAction(e -> goToShowtime());
    }

    // 🔙 Quay về Home
    private void goBackHome() {
        try {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(
                    getClass().getResource("/ui/view/home.fxml")));
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🎟 Chuyển sang chọn suất chiếu (flow chuẩn app cinema)
    private void goToShowtime() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/view/showtime.fxml")
            );

            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnBook.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔗 Cho phép HomeController truyền movieId (chuẩn MVC)
    public void setMovieId(int movieId) {
        this.movieId = movieId;
        loadMovieFromDB();
    }

    @FXML
    private void handleBack() { goBackHome(); }

    @FXML
    private void handleBookTicket() { goToShowtime(); }
}