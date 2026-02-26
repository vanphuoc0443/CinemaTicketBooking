package fxcontroller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class HomeController {

    @FXML
    private HBox movieContainer;

    @FXML
    private HBox upcomingContainer;

    @FXML
    private TextField txtSearch;

    private final List<Movie> nowShowingList = new ArrayList<>();

    @FXML
    public void initialize() {
        loadNowShowingMovies();
        loadUpcomingMovies();
        setupSearch();
    }

    // ================= LOAD PHIM ĐANG CHIẾU =================
    private void loadNowShowingMovies() {
        String sql = "SELECT * FROM movies WHERE status = 'NOW_SHOWING'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            movieContainer.getChildren().clear();
            nowShowingList.clear();

            while (rs.next()) {
                Movie movie = new Movie(
                        rs.getInt("movie_id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getString("poster_url")
                );

                nowShowingList.add(movie);
                movieContainer.getChildren().add(createMovieCard(movie));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= LOAD PHIM SẮP CHIẾU =================
    private void loadUpcomingMovies() {
        String sql = "SELECT * FROM movies WHERE status = 'UPCOMING'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            upcomingContainer.getChildren().clear();

            while (rs.next()) {
                Movie movie = new Movie(
                        rs.getInt("movie_id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getString("poster_url")
                );

                upcomingContainer.getChildren().add(createMovieCard(movie));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SEARCH REALTIME (GIỐNG APP CGV) =================
    private void setupSearch() {
        if (txtSearch == null) return;

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterMovies(newVal));
    }

    private void filterMovies(String keyword) {
        movieContainer.getChildren().clear();

        if (keyword == null || keyword.isEmpty()) {
            for (Movie movie : nowShowingList) {
                movieContainer.getChildren().add(createMovieCard(movie));
            }
            return;
        }

        String lower = keyword.toLowerCase();

        for (Movie movie : nowShowingList) {
            if (movie.getTitle().toLowerCase().contains(lower)) {
                movieContainer.getChildren().add(createMovieCard(movie));
            }
        }
    }

    // ================= CARD UI GIỐNG APP RẠP PHIM =================
    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox();
        card.setSpacing(8);
        card.setPrefWidth(150);
        card.setStyle("""
                -fx-background-color: #1E293B;
                -fx-background-radius: 18;
                -fx-padding: 12;
                -fx-cursor: hand;
                """);

        // Shadow cinematic
        DropShadow shadow = new DropShadow();
        shadow.setRadius(18);
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        card.setEffect(shadow);

        // Poster
        ImageView poster = new ImageView();
        poster.setFitWidth(130);
        poster.setFitHeight(185);
        poster.setPreserveRatio(false);

        try {
            if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
                poster.setImage(new Image(movie.getPosterUrl(), true));
            } else {
                poster.setImage(new Image(
                        getClass().getResource("/images/default_poster.png").toExternalForm()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Hover animation (giống Netflix/CGV)
        card.setOnMouseEntered(e -> {
            card.setScaleX(1.07);
            card.setScaleY(1.07);
        });

        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        // Click vào phim (chuyển sang trang đặt vé sau)
        card.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> openMovie(movie));

        Label title = new Label(movie.getTitle());
        title.setWrapText(true);
        title.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                """);

        Label genre = new Label("🎟 " + movie.getGenre());
        genre.setStyle("""
                -fx-text-fill: #F43F5E;
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                """);

        card.getChildren().addAll(poster, title, genre);
        return card;
    }

    // ================= HANDLE CLICK MOVIE =================
    // ================= HANDLE CLICK MOVIE (CHUẨN APP CINEMA) =================
    private void openMovie(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/view/movie_detail.fxml")
            );

            Parent root = loader.load();

            // Lấy controller của MovieDetail
            MovieDetailController controller = loader.getController();

            // Truyền movieId sang màn chi tiết (CỰC QUAN TRỌNG)
            controller.setMovieId(movie.getId());

            // Chuyển scene
            Stage stage = (Stage) movieContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ================= MODEL NỘI BỘ =================
    static class Movie {
        private final int id;
        private final String title;
        private final String genre;
        private final String posterUrl;

        public Movie(int id, String title, String genre, String posterUrl) {
            this.id = id;
            this.title = title;
            this.genre = genre;
            this.posterUrl = posterUrl;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getGenre() { return genre; }
        public String getPosterUrl() { return posterUrl; }
    }
}