package fxcontroller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Movie;

public class MovieCardController {

    @FXML
    private StackPane root;

    @FXML
    private ImageView imgPoster;

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblGenre;

    @FXML
    private Label lblDuration;

    @FXML
    private Label lblRating;

    @FXML
    private Button btnBook;

    private Movie movie; // Nhận data từ HomeController

    @FXML
    public void initialize() {
        setupHoverEffect();
        setupClickCard();
    }

    // 🎬 SET DATA TỪ HOME (QUAN TRỌNG)
    public void setMovie(Movie movie) {
        this.movie = movie;

        lblTitle.setText(movie.getTitle());
        lblGenre.setText(movie.getGenre());
        lblDuration.setText(movie.getDuration() + " phút");

        // Rating fake (nếu DB chưa có)
        lblRating.setText("⭐ 8.5");

        // Load poster
        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
            imgPoster.setImage(new Image(movie.getPosterUrl(), true));
        }
    }

    // 🎟 Click nút Đặt vé (đi tới Movie Detail)
    @FXML
    private void handleBook() {
        goToMovieDetail();
    }

    // 🖱 Click cả card cũng mở chi tiết (giống CGV)
    private void setupClickCard() {
        root.setOnMouseClicked(e -> goToMovieDetail());
    }

    // ✨ Hover effect phóng to card (UI xịn)
    private void setupHoverEffect() {
        root.setOnMouseEntered(e -> {
            root.setScaleX(1.05);
            root.setScaleY(1.05);
        });

        root.setOnMouseExited(e -> {
            root.setScaleX(1.0);
            root.setScaleY(1.0);
        });
    }

    // 🎬 Chuyển sang Movie Detail (Flow chuẩn cinema app)
    private void goToMovieDetail() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/view/movie_detail.fxml")
            );

            Scene scene = new Scene(loader.load());

            // Truyền movieId sang MovieDetailController
            MovieDetailController controller = loader.getController();
            if (movie != null) {
                controller.setMovieId(movie.getMovieId());
            }

            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}