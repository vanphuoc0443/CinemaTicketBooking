package fxcontroller;

import dao.MovieDAO;
import dao.ShowtimeDAO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Movie;
import service.OmdbApiService;
import util.Session;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HomeController {

    @FXML
    private TextField searchField;
    @FXML
    private FlowPane movieContainer;
    @FXML
    private FlowPane comingSoonContainer;
    @FXML
    private Label sectionTitle;
    @FXML
    private Label statusLabel;
    @FXML
    private Label comingSoonTitle;
    @FXML
    private Label comingSoonCount;
    @FXML
    private ProgressIndicator loadingIndicator;

    private final OmdbApiService apiService = new OmdbApiService();
    private final MovieDAO movieDAO = new MovieDAO();
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();

    // Store the currently selected movie for passing to next screen
    public static Movie selectedMovie = null;

    @FXML
    public void initialize() {
        loadLocalMovies();
    }

    @FXML
    public void searchMovies(ActionEvent event) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadLocalMovies();
            if (sectionTitle != null)
                sectionTitle.setText("🎥 Đang chiếu");
        } else {
            searchLocalAndOmdb(query);
            if (sectionTitle != null)
                sectionTitle.setText("🔍 Kết quả cho \"" + query + "\"");
        }
    }

    /**
     * Load movies from LOCAL DB and split into two sections:
     * - Now Showing: movies with showtimes (bookable)
     * - Coming Soon: movies without showtimes (not bookable yet)
     */
    private void loadLocalMovies() {
        if (loadingIndicator != null)
            loadingIndicator.setVisible(true);
        if (statusLabel != null)
            statusLabel.setText("Đang tải phim...");
        movieContainer.getChildren().clear();
        comingSoonContainer.getChildren().clear();

        Task<List<Movie>> task = new Task<>() {
            @Override
            protected List<Movie> call() throws SQLException {
                return movieDAO.findAll();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Movie> movies = task.getValue();
            if (loadingIndicator != null)
                loadingIndicator.setVisible(false);

            if (movies.isEmpty()) {
                statusLabel.setText("Chưa có phim nào. Admin cần thêm phim trước.");
                if (comingSoonCount != null)
                    comingSoonCount.setText("");
                return;
            }

            List<Movie> nowShowing = new ArrayList<>();
            List<Movie> comingSoon = new ArrayList<>();

            for (Movie movie : movies) {
                try {
                    int count = showtimeDAO.countByMovieId(movie.getMovieId());
                    if (count > 0) {
                        nowShowing.add(movie);
                    } else {
                        comingSoon.add(movie);
                    }
                } catch (SQLException ex) {
                    comingSoon.add(movie);
                }
            }

            // Now Showing section
            statusLabel.setText(nowShowing.size() + " phim đang chiếu");
            for (Movie movie : nowShowing) {
                movieContainer.getChildren().add(createMovieCard(movie, true));
            }
            if (nowShowing.isEmpty()) {
                Label empty = new Label("Chưa có phim nào đang chiếu");
                empty.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");
                movieContainer.getChildren().add(empty);
            }

            // Coming Soon section
            if (comingSoonCount != null)
                comingSoonCount.setText(comingSoon.size() + " phim sắp chiếu");
            for (Movie movie : comingSoon) {
                comingSoonContainer.getChildren().add(createMovieCard(movie, false));
            }
            if (comingSoon.isEmpty()) {
                Label empty = new Label("Không có phim nào sắp chiếu");
                empty.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");
                comingSoonContainer.getChildren().add(empty);
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            if (loadingIndicator != null)
                loadingIndicator.setVisible(false);
            statusLabel.setText("⚠ Lỗi tải phim từ DB.");
            task.getException().printStackTrace();
        }));

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Search: local DB first, then OMDB. Show results in Now Showing section.
     */
    private void searchLocalAndOmdb(String query) {
        if (loadingIndicator != null)
            loadingIndicator.setVisible(true);
        if (statusLabel != null)
            statusLabel.setText("Đang tìm kiếm...");
        movieContainer.getChildren().clear();
        comingSoonContainer.getChildren().clear();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 1. Search local DB
                List<Movie> localMovies = movieDAO.searchByKeyword(query);
                Platform.runLater(() -> {
                    for (Movie m : localMovies) {
                        try {
                            int count = showtimeDAO.countByMovieId(m.getMovieId());
                            movieContainer.getChildren().add(createMovieCard(m, count > 0));
                        } catch (SQLException e) {
                            movieContainer.getChildren().add(createMovieCard(m, false));
                        }
                    }
                });

                // 2. Search OMDB for additional results
                List<Movie> omdbMovies = apiService.searchMovies(query);
                for (Movie m : omdbMovies) {
                    boolean alreadyLocal = false;
                    for (Movie lm : localMovies) {
                        if (lm.getTitle().equalsIgnoreCase(m.getTitle())) {
                            alreadyLocal = true;
                            break;
                        }
                    }
                    if (alreadyLocal)
                        continue;

                    if (m.getImdbId() != null && !m.getImdbId().isEmpty()) {
                        Movie detailed = apiService.getMovieDetails(m.getImdbId());
                        if (detailed != null) {
                            m.setImdbRating(detailed.getImdbRating());
                            m.setGenre(detailed.getGenre());
                            m.setDescription(detailed.getDescription());
                            m.setDuration(detailed.getDuration());
                            m.setDirector(detailed.getDirector());
                            m.setLanguage(detailed.getLanguage());
                        }
                    }

                    Platform.runLater(() -> movieContainer.getChildren().add(createMovieCard(m, false)));
                }

                int total = localMovies.size() + omdbMovies.size();
                Platform.runLater(() -> {
                    if (loadingIndicator != null)
                        loadingIndicator.setVisible(false);
                    statusLabel.setText(total + " phim được tìm thấy");
                });
                return null;
            }
        };

        task.setOnFailed(e -> Platform.runLater(() -> {
            if (loadingIndicator != null)
                loadingIndicator.setVisible(false);
            statusLabel.setText("⚠ Lỗi tìm kiếm.");
        }));

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Build a styled movie card with poster image.
     * 
     * @param hasShowtimes true → bookable, false → coming soon style
     */
    private VBox createMovieCard(Movie movie, boolean hasShowtimes) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("card-movie");
        card.setPadding(new Insets(0, 0, 12, 0));
        card.setPrefWidth(200);

        // Poster image — always load
        ImageView poster = new ImageView();
        poster.setFitWidth(180);
        poster.setFitHeight(260);
        poster.setPreserveRatio(true);
        poster.getStyleClass().add("movie-poster");

        String posterUrl = movie.getPosterUrl();
        if (posterUrl != null && !posterUrl.equals("N/A") && posterUrl.startsWith("http")) {
            Image image = new Image(posterUrl, 180, 260, true, true, true);
            poster.setImage(image);
        } else {
            poster.setStyle("-fx-background-color: #37474f;");
        }

        // Title + rating row
        HBox titleRow = new HBox(6);
        titleRow.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(movie.getTitle());
        titleLabel.getStyleClass().add("heading-sm");
        titleLabel.setMaxWidth(160);
        titleLabel.setWrapText(false);
        titleRow.getChildren().add(titleLabel);

        String rating = movie.getImdbRating();
        if (rating != null && !rating.equals("N/A")) {
            Label ratingLabel = new Label("⭐ " + rating);
            ratingLabel.getStyleClass().add("rating-badge");
            titleRow.getChildren().add(ratingLabel);
        }

        // Genre + duration
        String info = "";
        if (movie.getGenre() != null && !movie.getGenre().equals("N/A")) {
            String genre = movie.getGenre().split(",")[0].trim();
            info = genre;
        }
        if (movie.getDuration() > 0) {
            int hours = movie.getDuration() / 60;
            int mins = movie.getDuration() % 60;
            info += " • " + (hours > 0 ? hours + "h " : "") + mins + "m";
        }
        Label infoLabel = new Label(info.isEmpty() ? "Movie" : info);
        infoLabel.getStyleClass().add("text-secondary");

        // Description tooltip
        if (movie.getDescription() != null && !movie.getDescription().isEmpty()) {
            Tooltip tooltip = new Tooltip(movie.getDescription());
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(300);
            Tooltip.install(card, tooltip);
        }

        card.getChildren().addAll(poster, titleRow, infoLabel);

        if (hasShowtimes) {
            // Now Showing — bookable
            try {
                int count = showtimeDAO.countByMovieId(movie.getMovieId());
                Label bookLabel = new Label("🎬 " + count + " suất chiếu • Đặt vé →");
                bookLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 11px; -fx-font-weight: bold;");
                card.getChildren().add(bookLabel);
            } catch (SQLException e) {
                Label bookLabel = new Label("🎬 Đặt vé →");
                bookLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 11px; -fx-font-weight: bold;");
                card.getChildren().add(bookLabel);
            }

            card.setOnMouseClicked(event -> {
                selectedMovie = movie;
                SceneManager.switchScene("movie_detail.fxml");
            });
            card.setStyle("-fx-cursor: hand;");
        } else {
            // Coming Soon — not bookable
            Label comingLabel = new Label("🎞 Sắp chiếu");
            comingLabel.setStyle("-fx-text-fill: #8b5cf6; -fx-font-size: 11px; -fx-font-weight: bold;");
            card.getChildren().add(comingLabel);
            card.setOpacity(0.75);
        }

        return card;
    }

    @FXML
    public void openMovieDetail(Event event) {
        SceneManager.switchScene("movie_detail.fxml");
    }

    @FXML
    public void goProfile(ActionEvent event) {
        SceneManager.switchScene("profile.fxml");
    }

    @FXML
    public void goHistory(ActionEvent event) {
        SceneManager.switchScene("history.fxml");
    }

    @FXML
    public void logout(ActionEvent event) {
        Session.clear();
        selectedMovie = null;
        SceneManager.switchScene("login.fxml");
    }
}
