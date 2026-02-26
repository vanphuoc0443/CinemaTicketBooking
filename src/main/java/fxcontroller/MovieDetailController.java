package fxcontroller;

import dao.ShowtimeDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Movie;
import model.Showtime;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MovieDetailController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label directorLabel;
    @FXML
    private Label genreLabel;
    @FXML
    private Label releaseDateLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label ratingBadge;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label expirationLabel;
    @FXML
    private ImageView posterImage;
    @FXML
    private HBox dateTabs;
    @FXML
    private FlowPane timeSlots;
    @FXML
    private Button bookBtn;

    private Button selectedDateBtn = null;
    private Button selectedTimeBtn = null;

    // Real showtime data from DB
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private List<Showtime> currentShowtimes = new ArrayList<>();
    private Showtime selectedShowtimeObj = null;

    // Store selected values for passing to next screen
    public static String selectedShowtime = null;
    public static String selectedDate = null;
    public static int selectedShowtimeId = -1;

    @FXML
    public void initialize() {
        Movie movie = HomeController.selectedMovie;
        if (movie != null) {
            populateMovieInfo(movie);
            buildDateTabs();
        }
        updateBookButton();
    }

    // ------- Populate movie info -------
    private void populateMovieInfo(Movie movie) {
        if (titleLabel != null)
            titleLabel.setText(movie.getTitle());
        if (directorLabel != null)
            directorLabel.setText(movie.getDirector() != null ? movie.getDirector() : "N/A");
        if (genreLabel != null)
            genreLabel.setText(movie.getGenre() != null ? movie.getGenre() : "N/A");

        if (releaseDateLabel != null) {
            if (movie.getReleaseDate() != null) {
                releaseDateLabel.setText(new SimpleDateFormat("dd/MM/yyyy").format(movie.getReleaseDate()));
            } else {
                releaseDateLabel.setText("N/A");
            }
        }

        if (durationLabel != null) {
            if (movie.getDuration() > 0) {
                int h = movie.getDuration() / 60;
                int m = movie.getDuration() % 60;
                durationLabel.setText((h > 0 ? h + "h " : "") + m + " phút");
            } else {
                durationLabel.setText("N/A");
            }
        }

        if (languageLabel != null)
            languageLabel.setText(movie.getLanguage() != null ? movie.getLanguage() : "N/A");

        if (ratingLabel != null && ratingBadge != null) {
            String rating = movie.getImdbRating();
            if (rating != null && !rating.equals("N/A")) {
                ratingLabel.setText("IMDb");
                ratingBadge.setText("⭐ " + rating);
            } else {
                ratingLabel.setText("");
                ratingBadge.setText("");
            }
        }

        if (descriptionLabel != null)
            descriptionLabel.setText(movie.getDescription() != null ? movie.getDescription() : "");

        // Poster
        if (posterImage != null) {
            String url = movie.getPosterUrl();
            if (url != null && !url.equals("N/A") && url.startsWith("http")) {
                posterImage.setImage(new Image(url, 300, 450, true, true, true));
            }
        }
    }

    // ------- Build date tabs (today + next 6 days) -------
    private void buildDateTabs() {
        dateTabs.getChildren().clear();
        LocalDate today = LocalDate.now();
        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("vi"));
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            String dayName = i == 0 ? "Hôm nay" : date.format(dayFormat);
            String dateStr = date.format(dateFormat);

            VBox tab = new VBox(2);
            tab.setAlignment(Pos.CENTER);
            tab.setPadding(new Insets(8, 14, 8, 14));

            Label dayLabel = new Label(dayName);
            dayLabel.setStyle("-fx-text-fill: #8893a7; -fx-font-size: 11px;");
            Label dateLabel = new Label(dateStr);
            dateLabel.setStyle("-fx-text-fill: #eef0f4; -fx-font-size: 14px; -fx-font-weight: bold;");

            tab.getChildren().addAll(dayLabel, dateLabel);

            Button tabBtn = new Button();
            tabBtn.setGraphic(tab);
            tabBtn.getStyleClass().add("date-tab-btn");
            tabBtn.setUserData(date.toString());

            tabBtn.setOnAction(e -> selectDate(tabBtn));

            dateTabs.getChildren().add(tabBtn);

            // Auto-select today
            if (i == 0) {
                selectDate(tabBtn);
            }
        }
    }

    private void selectDate(Button btn) {
        if (selectedDateBtn != null) {
            selectedDateBtn.getStyleClass().remove("date-tab-selected");
        }
        selectedDateBtn = btn;
        btn.getStyleClass().add("date-tab-selected");
        selectedDate = (String) btn.getUserData();

        // Reset time selection
        selectedTimeBtn = null;
        selectedShowtimeObj = null;
        selectedShowtime = null;
        selectedShowtimeId = -1;

        // Load real showtimes for this date
        buildTimeSlots();
        updateBookButton();
    }

    // ------- Build time slots from REAL DB data -------
    private void buildTimeSlots() {
        timeSlots.getChildren().clear();

        Movie movie = HomeController.selectedMovie;
        if (movie == null || selectedDate == null)
            return;

        try {
            // Load all showtimes for this movie on the selected date
            currentShowtimes = showtimeDAO.findByMovieAndDate(movie.getMovieId(), selectedDate);

            if (currentShowtimes.isEmpty()) {
                Label noSlots = new Label("Không có suất chiếu cho ngày này");
                noSlots.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
                timeSlots.getChildren().add(noSlots);
                return;
            }

            for (Showtime st : currentShowtimes) {
                // Format: "10:00 - Phòng 1"
                String timeStr = st.getShowTime();
                if (timeStr != null && timeStr.length() > 5) {
                    timeStr = timeStr.substring(0, 5); // Trim seconds "10:00:00" → "10:00"
                }
                String roomName = "P." + st.getRoomNumber();
                String label = timeStr + "\n" + roomName;

                Button btn = new Button(label);
                btn.getStyleClass().add("timeslot-btn");
                btn.setPrefWidth(100);
                btn.setPrefHeight(50);

                // Check if seats available
                if (st.getAvailableSeats() <= 0) {
                    btn.setDisable(true);
                    btn.setStyle("-fx-opacity: 0.4;");
                    btn.setText(timeStr + "\nHết vé");
                } else {
                    String seatsInfo = st.getAvailableSeats() + "/" + st.getTotalSeats() + " ghế";
                    javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(seatsInfo);
                    javafx.scene.control.Tooltip.install(btn, tooltip);

                    final Showtime showtime = st;
                    final String finalTime = timeStr;
                    btn.setOnAction(e -> selectTime(btn, showtime, finalTime));
                }

                timeSlots.getChildren().add(btn);
            }

        } catch (SQLException e) {
            Label errorLabel = new Label("⚠ Lỗi tải suất chiếu");
            errorLabel.setStyle("-fx-text-fill: #ef4444;");
            timeSlots.getChildren().add(errorLabel);
        }
    }

    private void selectTime(Button btn, Showtime showtime, String timeStr) {
        if (selectedTimeBtn != null) {
            selectedTimeBtn.getStyleClass().remove("timeslot-selected");
        }
        selectedTimeBtn = btn;
        btn.getStyleClass().add("timeslot-selected");

        selectedShowtimeObj = showtime;
        selectedShowtime = timeStr;
        selectedShowtimeId = showtime.getShowtimeId();

        // Update expiration display
        expirationLabel.setText("Vé có hiệu lực đến 30 phút sau suất chiếu " +
                selectedShowtime + " • Phòng " + showtime.getRoomNumber());
        updateBookButton();
    }

    // ------- Update book button state -------
    private void updateBookButton() {
        bookBtn.setDisable(selectedDateBtn == null || selectedTimeBtn == null || selectedShowtimeObj == null);
    }

    // ------- Navigation -------
    @FXML
    public void goBack(ActionEvent event) {
        SceneManager.switchScene("home.fxml");
    }

    @FXML
    public void bookTicket(ActionEvent event) {
        if (selectedDateBtn == null || selectedTimeBtn == null || selectedShowtimeObj == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Chưa chọn suất chiếu");
            alert.setContentText("Vui lòng chọn ngày và suất chiếu trước khi đặt vé!");
            alert.show();
            return;
        }
        // Init booking session with REAL showtime ID from DB
        util.BookingSession.start(selectedShowtimeObj.getShowtimeId());
        SceneManager.switchScene("seat.fxml");
    }
}
