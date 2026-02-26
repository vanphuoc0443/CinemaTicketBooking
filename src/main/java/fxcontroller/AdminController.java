package fxcontroller;

import dao.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.*;
import service.OmdbApiService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminController — quản lý phòng chiếu, phim, suất chiếu.
 * Hoàn toàn tách biệt khỏi ứng dụng người dùng.
 */
public class AdminController {

    // --- Tab 1: Theaters ---
    @FXML
    private ListView<String> theaterList;
    @FXML
    private Label theaterCountLabel;

    // --- Tab 2: Movies ---
    @FXML
    private TextField movieSearchField;
    @FXML
    private ListView<String> searchResultList;
    @FXML
    private ListView<String> scheduledMovieList;

    // --- Tab 3: Showtimes ---
    @FXML
    private ComboBox<String> movieCombo;
    @FXML
    private ComboBox<String> theaterCombo;
    @FXML
    private DatePicker showDatePicker;
    @FXML
    private ComboBox<String> timeCombo;
    @FXML
    private Label endTimeLabel;
    @FXML
    private ListView<String> showtimeList;
    @FXML
    private Label showtimeCountLabel;

    // --- General ---
    @FXML
    private Label statusLabel;
    @FXML
    private TabPane mainTabPane;

    // DAOs
    private final TheaterDAO theaterDAO = new TheaterDAO();
    private final MovieDAO movieDAO = new MovieDAO();
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private final SeatDAO seatDAO = new SeatDAO();
    private final OmdbApiService omdbApi = new OmdbApiService();

    // In-memory data
    private List<Theater> theaters = new ArrayList<>();
    private List<Movie> searchResults = new ArrayList<>();
    private List<Movie> scheduledMovies = new ArrayList<>();
    private List<Showtime> showtimes = new ArrayList<>();
    private final List<String> allTimeSlots = new ArrayList<>();

    private static final int MAX_THEATERS = 10;
    private static final int MAX_SHOWTIMES_PER_MOVIE = 10;

    @FXML
    public void initialize() {
        // Build all time slots: 08:00 to 23:00, every 30 min
        for (int h = 8; h <= 23; h++) {
            allTimeSlots.add(String.format("%02d:00", h));
            if (h < 23) {
                allTimeSlots.add(String.format("%02d:30", h));
            }
        }
        timeCombo.getItems().addAll(allTimeSlots);

        showDatePicker.setValue(LocalDate.now());

        // Auto-refresh available time slots when movie/theater/date changes
        movieCombo.setOnAction(e -> {
            updateEndTime();
            refreshAvailableTimeSlots();
        });
        theaterCombo.setOnAction(e -> refreshAvailableTimeSlots());
        showDatePicker.setOnAction(e -> refreshAvailableTimeSlots());
        timeCombo.setOnAction(e -> updateEndTime());

        // Load initial data
        loadTheaters();
        loadScheduledMovies();
        loadShowtimes();

        setStatus("✅ Admin sẵn sàng");
    }

    /**
     * Khi admin chọn phim + phòng + ngày, tự động lọc các khung giờ:
     * - ✅ Giờ trống → hiện bình thường
     * - ❌ Giờ bị trùng → hiện dấu ❌ và ghi chú phim trùng
     */
    private void refreshAvailableTimeSlots() {
        int movieIdx = movieCombo.getSelectionModel().getSelectedIndex();
        int theaterIdx = theaterCombo.getSelectionModel().getSelectedIndex();
        LocalDate date = showDatePicker.getValue();

        // Need all 3 selected to filter
        if (movieIdx < 0 || theaterIdx < 0 || date == null ||
                movieIdx >= scheduledMovies.size() || theaterIdx >= theaters.size()) {
            timeCombo.getItems().clear();
            timeCombo.getItems().addAll(allTimeSlots);
            return;
        }

        Movie movie = scheduledMovies.get(movieIdx);
        Theater theater = theaters.get(theaterIdx);
        int duration = movie.getDuration() > 0 ? movie.getDuration() : 120;

        String prevSelection = timeCombo.getValue();
        timeCombo.getItems().clear();

        for (String slot : allTimeSlots) {
            try {
                boolean conflict = showtimeDAO.checkTimeOverlap(
                        theater.getTheaterId(), date.toString(), slot, duration, -1);
                if (conflict) {
                    timeCombo.getItems().add(slot + "  ❌ trùng lịch");
                } else {
                    timeCombo.getItems().add(slot + "  ✅");
                }
            } catch (SQLException e) {
                timeCombo.getItems().add(slot);
            }
        }

        // Try to restore previous selection
        if (prevSelection != null) {
            for (String item : timeCombo.getItems()) {
                if (item.startsWith(prevSelection.split(" ")[0])) {
                    timeCombo.setValue(item);
                    break;
                }
            }
        }

        setStatus("📅 Đã cập nhật khung giờ cho " + theater.getName() + " ngày " + date);
    }

    // ============================================================
    // TAB 1: THEATER MANAGEMENT
    // ============================================================

    @FXML
    public void addTheater() {
        try {
            int count = theaterDAO.countActive();
            if (count >= MAX_THEATERS) {
                showAlert("Giới hạn", "Đã đạt tối đa " + MAX_THEATERS + " phòng chiếu!");
                return;
            }

            int nextNum = count + 1;
            Theater t = new Theater("Phòng " + nextNum);
            theaterDAO.save(t);

            setStatus("✅ Đã thêm " + t.getName());
            loadTheaters();
            refreshTheaterCombo();
        } catch (SQLException e) {
            showError("Lỗi thêm phòng: " + e.getMessage());
        }
    }

    @FXML
    public void deleteTheater() {
        int idx = theaterList.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= theaters.size()) {
            showAlert("Thông báo", "Vui lòng chọn phòng cần xóa.");
            return;
        }

        Theater t = theaters.get(idx);

        try {
            if (theaterDAO.hasActiveShowtimes(t.getTheaterId())) {
                showAlert("Không thể xóa", t.getName() + " đang có suất chiếu.\nVui lòng xóa hết suất chiếu trước.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Xóa " + t.getName() + "?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES)
                return;

            theaterDAO.delete(t.getTheaterId());
            setStatus("🗑 Đã xóa " + t.getName());
            loadTheaters();
            refreshTheaterCombo();
        } catch (SQLException e) {
            showError("Lỗi xóa phòng: " + e.getMessage());
        }
    }

    private void loadTheaters() {
        try {
            theaters = theaterDAO.findAll();
            theaterList.getItems().clear();
            for (Theater t : theaters) {
                String info = t.getName() + "  •  " + t.getTotalSeats() + " ghế";
                theaterList.getItems().add(info);
            }
            theaterCountLabel.setText(theaters.size() + "/" + MAX_THEATERS + " phòng");
            refreshTheaterCombo();
        } catch (SQLException e) {
            showError("Lỗi tải phòng: " + e.getMessage());
        }
    }

    private void refreshTheaterCombo() {
        String prev = theaterCombo.getValue();
        theaterCombo.getItems().clear();
        for (Theater t : theaters) {
            theaterCombo.getItems().add(t.getName());
        }
        if (prev != null && theaterCombo.getItems().contains(prev)) {
            theaterCombo.setValue(prev);
        }
    }

    // ============================================================
    // TAB 2: MOVIE MANAGEMENT
    // ============================================================

    @FXML
    public void searchMoviesForAdmin() {
        String query = movieSearchField.getText().trim();
        if (query.isEmpty()) {
            showAlert("Thông báo", "Vui lòng nhập tên phim để tìm.");
            return;
        }

        setStatus("🔍 Đang tìm kiếm '" + query + "'...");
        searchResultList.getItems().clear();

        Task<List<Movie>> task = new Task<>() {
            @Override
            protected List<Movie> call() {
                List<Movie> results = omdbApi.searchMovies(query);
                // Get details for each
                for (Movie m : results) {
                    if (m.getImdbId() != null && !m.getImdbId().isEmpty()) {
                        Movie detailed = omdbApi.getMovieDetails(m.getImdbId());
                        if (detailed != null) {
                            m.setDuration(detailed.getDuration());
                            m.setGenre(detailed.getGenre());
                            m.setDescription(detailed.getDescription());
                            m.setDirector(detailed.getDirector());
                        }
                    }
                }
                return results;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            searchResults = task.getValue();
            searchResultList.getItems().clear();
            for (Movie m : searchResults) {
                String dur = m.getDuration() > 0 ? " (" + m.getDuration() + " phút)" : "";
                searchResultList.getItems().add(m.getTitle() + dur);
            }
            setStatus("✅ Tìm thấy " + searchResults.size() + " phim");
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            showError("Lỗi tìm kiếm: " + task.getException().getMessage());
        }));

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void addMovieToSchedule() {
        int idx = searchResultList.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= searchResults.size()) {
            showAlert("Thông báo", "Vui lòng chọn phim từ kết quả tìm kiếm.");
            return;
        }

        Movie movie = searchResults.get(idx);

        // Fetch FULL details from OMDB (director, rating, language, description...)
        if (movie.getImdbId() != null && !movie.getImdbId().isEmpty()) {
            Movie detailed = omdbApi.getMovieDetails(movie.getImdbId());
            if (detailed != null) {
                movie.setGenre(detailed.getGenre());
                movie.setDescription(detailed.getDescription());
                movie.setDuration(detailed.getDuration());
                movie.setDirector(detailed.getDirector());
                movie.setImdbRating(detailed.getImdbRating());
                movie.setLanguage(detailed.getLanguage());
                if (detailed.getReleaseDate() != null) {
                    movie.setReleaseDate(detailed.getReleaseDate());
                }
                if (detailed.getPosterUrl() != null && !detailed.getPosterUrl().equals("N/A")) {
                    movie.setPosterUrl(detailed.getPosterUrl());
                }
            }
        }

        // Check if already in DB
        try {
            List<Movie> existing = movieDAO.searchByKeyword(movie.getTitle());
            for (Movie m : existing) {
                if (m.getTitle().equalsIgnoreCase(movie.getTitle())) {
                    showAlert("Đã tồn tại", "Phim \"" + movie.getTitle() + "\" đã có trong lịch chiếu.");
                    return;
                }
            }

            // Duration fallback
            if (movie.getDuration() <= 0) {
                movie.setDuration(120); // default 2h
            }

            movieDAO.save(movie);
            setStatus("✅ Đã thêm \"" + movie.getTitle() + "\" vào lịch chiếu");
            loadScheduledMovies();
        } catch (SQLException e) {
            showError("Lỗi thêm phim: " + e.getMessage());
        }
    }

    @FXML
    public void removeMovie() {
        int idx = scheduledMovieList.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= scheduledMovies.size()) {
            showAlert("Thông báo", "Vui lòng chọn phim cần xóa.");
            return;
        }

        Movie movie = scheduledMovies.get(idx);

        try {
            // Check for active showtimes
            int showtimeCount = showtimeDAO.countByMovieId(movie.getMovieId());
            if (showtimeCount > 0) {
                showAlert("Không thể xóa",
                        "\"" + movie.getTitle() + "\" đang có " + showtimeCount +
                                " suất chiếu.\nVui lòng xóa hết suất chiếu trước.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Xóa phim \"" + movie.getTitle() + "\" khỏi lịch?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES)
                return;

            movieDAO.delete(movie.getMovieId());
            setStatus("🗑 Đã xóa \"" + movie.getTitle() + "\"");
            loadScheduledMovies();
        } catch (SQLException e) {
            showError("Lỗi xóa phim: " + e.getMessage());
        }
    }

    private void loadScheduledMovies() {
        try {
            scheduledMovies = movieDAO.findAll();
            scheduledMovieList.getItems().clear();
            for (Movie m : scheduledMovies) {
                String dur = m.getDuration() > 0 ? " (" + m.getDuration() + " phút)" : "";
                String genre = m.getGenre() != null ? " • " + m.getGenre() : "";
                scheduledMovieList.getItems().add(m.getTitle() + dur + genre);
            }

            // Refresh movie combo
            String prev = movieCombo.getValue();
            movieCombo.getItems().clear();
            for (Movie m : scheduledMovies) {
                String dur = m.getDuration() > 0 ? " (" + m.getDuration() + " phút)" : "";
                movieCombo.getItems().add(m.getTitle() + dur);
            }
            if (prev != null && movieCombo.getItems().contains(prev)) {
                movieCombo.setValue(prev);
            }
        } catch (SQLException e) {
            showError("Lỗi tải phim: " + e.getMessage());
        }
    }

    // ============================================================
    // TAB 3: SHOWTIME MANAGEMENT
    // ============================================================

    @FXML
    public void createShowtime() {
        // Validate inputs
        int movieIdx = movieCombo.getSelectionModel().getSelectedIndex();
        if (movieIdx < 0 || movieIdx >= scheduledMovies.size()) {
            showAlert("Thiếu thông tin", "Vui lòng chọn phim.");
            return;
        }
        Movie movie = scheduledMovies.get(movieIdx);

        int theaterIdx = theaterCombo.getSelectionModel().getSelectedIndex();
        if (theaterIdx < 0 || theaterIdx >= theaters.size()) {
            showAlert("Thiếu thông tin", "Vui lòng chọn phòng chiếu.");
            return;
        }
        Theater theater = theaters.get(theaterIdx);

        LocalDate date = showDatePicker.getValue();
        if (date == null) {
            showAlert("Thiếu thông tin", "Vui lòng chọn ngày chiếu.");
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            showAlert("Lỗi", "Ngày chiếu không thể trong quá khứ.");
            return;
        }

        String rawTime = timeCombo.getValue();
        if (rawTime == null || rawTime.isEmpty()) {
            showAlert("Thiếu thông tin", "Vui lòng chọn giờ chiếu.");
            return;
        }
        // Extract base time from decorated value like "10:00 ✅" or "10:00 ❌ trùng lịch"
        String time = rawTime.split(" ")[0].trim();
        if (rawTime.contains("❌")) {
            showAlert("⚠ Xung đột", "Khung giờ " + time + " đã bị trùng lịch!\nVui lòng chọn giờ có dấu ✅.");
            return;
        }

        try {
            // Check max showtimes per movie
            int count = showtimeDAO.countByMovieId(movie.getMovieId());
            if (count >= MAX_SHOWTIMES_PER_MOVIE) {
                showAlert("Giới hạn",
                        "Phim \"" + movie.getTitle() + "\" đã đạt tối đa " +
                                MAX_SHOWTIMES_PER_MOVIE + " suất chiếu!");
                return;
            }

            // Check time overlap
            int duration = movie.getDuration() > 0 ? movie.getDuration() : 120;
            boolean hasConflict = showtimeDAO.checkTimeOverlap(
                    theater.getTheaterId(), date.toString(), time, duration, -1);

            if (hasConflict) {
                int endMin = parseTimeToMin(time) + duration + 15;
                String endTime = String.format("%02d:%02d", endMin / 60, endMin % 60);
                showAlert("⚠ Xung đột thời gian",
                        "Suất chiếu " + time + " → " + endTime +
                                " tại " + theater.getName() + " bị trùng với suất chiếu khác.\n" +
                                "Vui lòng chọn thời gian khác.");
                return;
            }

            // Create showtime
            Showtime st = new Showtime();
            st.setMovieId(movie.getMovieId());
            st.setShowDate(date.toString());
            st.setShowTime(time);
            st.setRoomNumber(theater.getTheaterId());
            st.setTotalSeats(theater.getTotalSeats());
            st.setAvailableSeats(theater.getTotalSeats());

            boolean saved = showtimeDAO.save(st);

            if (saved) {
                // Auto-generate seats
                generateSeatsForShowtime(st.getShowtimeId(), theater.getTotalSeats());

                setStatus("✅ Đã tạo suất chiếu: " + movie.getTitle() + " lúc " + time +
                        " tại " + theater.getName());
                loadShowtimes();
            } else {
                showError("Không thể tạo suất chiếu.");
            }

        } catch (SQLException e) {
            showError("Lỗi tạo suất chiếu: " + e.getMessage());
        }
    }

    @FXML
    public void deleteShowtime() {
        int idx = showtimeList.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= showtimes.size()) {
            showAlert("Thông báo", "Vui lòng chọn suất chiếu cần xóa.");
            return;
        }

        Showtime st = showtimes.get(idx);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa suất chiếu " + st.getMovieTitle() + " lúc " + st.getShowTime() +
                        "?\n\n⚠ Tất cả booking và ghế liên quan cũng sẽ bị xóa!",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Xác nhận xóa suất chiếu");
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES)
            return;

        try {
            cascadeDeleteShowtime(st.getShowtimeId());
            setStatus("🗑 Đã xóa suất chiếu #" + st.getShowtimeId());
            loadShowtimes();
        } catch (SQLException e) {
            showError("Lỗi xóa suất chiếu: " + e.getMessage());
        }
    }

    /**
     * Xóa suất chiếu kèm tất cả dữ liệu liên quan:
     * payments → booking_seats → bookings → seat_locks → seats (CASCADE) → showtime
     */
    private void cascadeDeleteShowtime(int showtimeId) throws SQLException {
        try (java.sql.Connection conn = util.DatabaseConnection.getConnection()) {
            // 1. Delete payments for bookings of this showtime
            String delPayments = "DELETE p FROM payments p " +
                    "INNER JOIN bookings b ON p.booking_id = b.booking_id " +
                    "WHERE b.showtime_id = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(delPayments)) {
                stmt.setInt(1, showtimeId);
                stmt.executeUpdate();
            }

            // 2. Delete booking_seats for bookings of this showtime
            String delBookingSeats = "DELETE bs FROM booking_seats bs " +
                    "INNER JOIN bookings b ON bs.booking_id = b.booking_id " +
                    "WHERE b.showtime_id = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(delBookingSeats)) {
                stmt.setInt(1, showtimeId);
                stmt.executeUpdate();
            }

            // 3. Delete bookings for this showtime
            String delBookings = "DELETE FROM bookings WHERE showtime_id = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(delBookings)) {
                stmt.setInt(1, showtimeId);
                stmt.executeUpdate();
            }

            // 4. Delete seat_locks for this showtime (if table exists)
            try {
                String delLocks = "DELETE FROM seat_locks WHERE showtime_id = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(delLocks)) {
                    stmt.setInt(1, showtimeId);
                    stmt.executeUpdate();
                }
            } catch (SQLException ignored) {
                // seat_locks table may not exist
            }

            // 5. Delete showtime (seats cascade via ON DELETE CASCADE)
            String delShowtime = "DELETE FROM showtimes WHERE showtime_id = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(delShowtime)) {
                stmt.setInt(1, showtimeId);
                stmt.executeUpdate();
            }

            conn.commit();
        }
    }

    @FXML
    public void refreshShowtimes() {
        loadShowtimes();
        loadScheduledMovies();
        loadTheaters();
        setStatus("🔄 Đã làm mới dữ liệu");
    }

    private void loadShowtimes() {
        try {
            showtimes = showtimeDAO.findAll();
            showtimeList.getItems().clear();

            for (Showtime st : showtimes) {
                String roomName = "Phòng " + st.getRoomNumber();
                // Try to find theater name
                for (Theater t : theaters) {
                    if (t.getTheaterId() == st.getRoomNumber()) {
                        roomName = t.getName();
                        break;
                    }
                }
                String movieTitle = st.getMovieTitle() != null ? st.getMovieTitle() : "Phim #" + st.getMovieId();
                String info = String.format("📅 %s  ⏰ %s  🏛 %s  🎬 %s  (còn %d/%d ghế)",
                        st.getShowDate(), st.getShowTime(), roomName,
                        movieTitle, st.getAvailableSeats(), st.getTotalSeats());
                showtimeList.getItems().add(info);
            }

            showtimeCountLabel.setText(showtimes.size() + " suất chiếu");
        } catch (SQLException e) {
            showError("Lỗi tải suất chiếu: " + e.getMessage());
        }
    }

    // ============================================================
    // SEAT AUTO-GENERATION
    // ============================================================

    private void generateSeatsForShowtime(int showtimeId, int totalSeats) {
        // Layout: rows A-H, seats 1-10 = 80 seats
        // A-D: STANDARD (50,000đ), E-G: VIP (100,000đ), H: COUPLE (150,000đ)
        String[] rows = { "A", "B", "C", "D", "E", "F", "G", "H" };
        int seatsPerRow = 10;
        List<Seat> seats = new ArrayList<>();

        for (String row : rows) {
            SeatType type;
            double price;
            if (row.compareTo("E") < 0) {
                type = SeatType.STANDARD;
                price = 50000;
            } else if (row.compareTo("H") < 0) {
                type = SeatType.VIP;
                price = 100000;
            } else {
                type = SeatType.COUPLE;
                price = 150000;
            }

            for (int n = 1; n <= seatsPerRow; n++) {
                Seat seat = new Seat();
                seat.setShowtimeId(showtimeId);
                seat.setSeatNumber(row + n);
                seat.setSeatType(type);
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setPrice(price);
                seat.setVersion(0);
                seats.add(seat);
            }
        }

        try {
            seatDAO.saveAll(seats);
            System.out.println("✅ Tạo " + seats.size() + " ghế cho suất chiếu #" + showtimeId);
        } catch (SQLException e) {
            System.err.println("⚠ Lỗi tạo ghế: " + e.getMessage());
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private void updateEndTime() {
        int movieIdx = movieCombo.getSelectionModel().getSelectedIndex();
        String time = timeCombo.getValue();

        if (movieIdx >= 0 && movieIdx < scheduledMovies.size() && time != null) {
            Movie m = scheduledMovies.get(movieIdx);
            int duration = m.getDuration() > 0 ? m.getDuration() : 120;
            int startMin = parseTimeToMin(time);
            int endMin = startMin + duration + 15;
            String endStr = String.format("%02d:%02d", endMin / 60, endMin % 60);
            endTimeLabel.setText("⏱ Kết thúc lúc: " + endStr +
                    " (phim " + duration + " phút + 15 phút dọn dẹp)");
        } else {
            endTimeLabel.setText("");
        }
    }

    private int parseTimeToMin(String time) {
        if (time == null)
            return 0;
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 +
                (parts.length > 1 ? Integer.parseInt(parts[1]) : 0);
    }

    private void setStatus(String msg) {
        if (statusLabel != null)
            statusLabel.setText(msg);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void showError(String msg) {
        setStatus("❌ " + msg);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
