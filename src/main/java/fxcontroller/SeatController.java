package fxcontroller;

import controller.BookingController;
import controller.EnhancedBookingController;
import controller.SeatStatusDTO;
import dao.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.util.Duration;
import model.Seat;
import model.SeatStatus;
import util.BookingSession;
import util.Session;

import java.util.*;

/**
 * SeatController — Loads seat layout from DB, uses seat_locks for reservation,
 * and falls back to seat.status when seat_locks table is unavailable.
 */
public class SeatController {

    @FXML
    private GridPane seatGrid;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Label screenLabel;
    @FXML
    private Label cinemaNameLabel;
    @FXML
    private Label cinemaDetailLabel;
    @FXML
    private Label countdownLabel;
    @FXML
    private Label countBadge;
    @FXML
    private Button continueBtn;
    @FXML
    private Button togglePanelBtn;
    @FXML
    private FlowPane seatChipsPane;

    // ------- Data model -------
    public static Set<String> selectedSeats = new LinkedHashSet<>();
    public static int totalPrice = 0;

    private final Map<String, Button> seatButtons = new HashMap<>();
    private final Map<String, String> seatTypes = new HashMap<>();
    private final Map<String, Integer> seatPrices = new HashMap<>();
    private boolean panelExpanded = true;
    private int countdownSeconds = 600; // 10 min
    private Timeline countdownTimeline;

    private EnhancedBookingController enhancedBooking;
    private boolean dbAvailable = false;
    private boolean seatLocksAvailable = false;
    private List<Seat> dbSeats = new ArrayList<>();

    @FXML
    public void initialize() {
        selectedSeats.clear();
        totalPrice = 0;

        // 1. Init DB access
        try {
            BookingController bc = new BookingController(
                    new BookingDAO(), new SeatDAO(), new ShowtimeDAO(), new TransactionManager());
            enhancedBooking = new EnhancedBookingController(bc);
            dbAvailable = true;
        } catch (Exception e) {
            System.err.println("⚠ DB init failed: " + e.getMessage());
        }

        // 2. Load seats from DB
        if (dbAvailable) {
            try {
                SeatDAO seatDAO = new SeatDAO();
                dbSeats = seatDAO.findByShowtime(BookingSession.getShowtimeId());
            } catch (Exception e) {
                System.err.println("⚠ Cannot load seats: " + e.getMessage());
                dbSeats = new ArrayList<>();
            }

            // 3. Try cleanup expired locks (test if seat_locks table exists)
            try {
                SeatLockDAO lockDAO = new SeatLockDAO();
                lockDAO.cleanupExpiredLocks(BookingSession.getShowtimeId());
                seatLocksAvailable = true;
            } catch (Exception e) {
                System.err.println("⚠ seat_locks table not available: " + e.getMessage());
                seatLocksAvailable = false;
            }
        }

        // 4. Build grid
        if (!dbSeats.isEmpty()) {
            buildSeatGridFromDB();
        } else {
            buildSeatGridFallback();
        }

        // 5. Update cinema info
        updateCinemaInfo();
        updateInfo();
        startCountdown();
    }

    // ------- Update cinema info labels -------
    private void updateCinemaInfo() {
        String date = MovieDetailController.selectedDate;
        String time = MovieDetailController.selectedShowtime;
        if (cinemaDetailLabel != null) {
            String detail = "";
            if (date != null)
                detail += date;
            if (time != null)
                detail += " - " + time;
            detail += " - Phòng 01";
            cinemaDetailLabel.setText(detail);
        }
    }

    // ============================================================
    // BUILD SEAT GRID FROM DATABASE
    // ============================================================
    private void buildSeatGridFromDB() {
        seatGrid.getChildren().clear();
        seatGrid.setAlignment(Pos.CENTER);

        // Build status map from seat_locks (if available)
        Map<Integer, String> lockStatusMap = new HashMap<>();
        if (seatLocksAvailable) {
            try {
                List<SeatStatusDTO> statuses = enhancedBooking.getSeatsStatus(
                        BookingSession.getShowtimeId(),
                        BookingSession.getSessionToken());
                for (SeatStatusDTO dto : statuses) {
                    lockStatusMap.put(dto.getSeatId(), dto.getStatus());
                }
            } catch (Exception e) {
                System.err.println("⚠ getSeatsStatus failed: " + e.getMessage());
            }
        }

        // Group by row letter
        Map<String, List<Seat>> rowMap = new TreeMap<>();
        for (Seat seat : dbSeats) {
            String row = seat.getSeatNumber().substring(0, 1);
            rowMap.computeIfAbsent(row, k -> new ArrayList<>()).add(seat);
        }

        int rowIdx = 0;
        for (Map.Entry<String, List<Seat>> entry : rowMap.entrySet()) {
            String rowLetter = entry.getKey();
            List<Seat> seats = entry.getValue();
            seats.sort(Comparator.comparing(s -> {
                String num = s.getSeatNumber().substring(1);
                return Integer.parseInt(num);
            }));

            // Row label
            Label rowLabel = new Label(rowLetter);
            rowLabel.getStyleClass().add("seat-row-label");
            rowLabel.setMinWidth(28);
            rowLabel.setAlignment(Pos.CENTER);
            seatGrid.add(rowLabel, 0, rowIdx);

            int col = 1;
            for (int i = 0; i < seats.size(); i++) {
                Seat seat = seats.get(i);

                // Aisle gaps
                if (i == 2 || i == 7) {
                    Region gap = new Region();
                    gap.setPrefWidth(16);
                    seatGrid.add(gap, col, rowIdx);
                    col++;
                }

                String seatNumber = seat.getSeatNumber();
                String num = seatNumber.substring(1);
                String type = seat.getSeatType().name();
                int price = (int) seat.getPrice();

                BookingSession.mapSeatNumber(seatNumber, seat.getSeatId());
                seatTypes.put(seatNumber, type);
                seatPrices.put(seatNumber, price);

                // Determine effective status
                String effectiveStatus = determineStatus(seat, lockStatusMap);

                Button btn = new Button(num);
                btn.getStyleClass().add("seat-btn");
                btn.setMinWidth(38);
                btn.setMinHeight(34);

                applySeatStyle(btn, seatNumber, type, effectiveStatus);

                if (type.equals("COUPLE")) {
                    btn.setMinWidth(80);
                    GridPane.setColumnSpan(btn, 2);
                    seatGrid.add(btn, col, rowIdx);
                    col += 2;
                } else {
                    seatGrid.add(btn, col, rowIdx);
                    col++;
                }

                seatButtons.put(seatNumber, btn);
            }
            rowIdx++;
        }
    }

    /**
     * Determine effective seat status:
     * Priority: DB seat.status > lock status > fallback to "available"
     */
    private String determineStatus(Seat seat, Map<Integer, String> lockStatusMap) {
        // 1. Check DB seat status first (most reliable)
        if (seat.getStatus() == SeatStatus.BOOKED) {
            return "booked";
        }
        if (seat.getStatus() == SeatStatus.RESERVED) {
            return "reserved";
        }

        // 2. Check lock status (if available)
        String lockStatus = lockStatusMap.get(seat.getSeatId());
        if (lockStatus != null) {
            return lockStatus; // "available", "locked-mine", "locked-others"
        }

        // 3. Default = available
        return "available";
    }

    /**
     * Apply visual style + click handler based on status.
     */
    private void applySeatStyle(Button btn, String seatNumber, String type, String status) {
        switch (status) {
            case "booked":
            case "reserved":
                btn.getStyleClass().add("seat-booked-dark");
                btn.setDisable(true);
                btn.setOpacity(0.35);
                Tooltip.install(btn, new Tooltip("Ghế " + seatNumber + " đã được đặt"));
                break;

            case "locked-others":
                btn.getStyleClass().add("seat-locked-others");
                btn.setDisable(true);
                btn.setOpacity(0.5);
                Tooltip.install(btn, new Tooltip("Ghế " + seatNumber + " đang được giữ bởi người khác"));
                break;

            case "locked-mine":
                btn.getStyleClass().add("seat-selected-orange");
                selectedSeats.add(seatNumber);
                Integer seatId = BookingSession.getSeatIdByNumber(seatNumber);
                if (seatId != null)
                    BookingSession.addSeatId(seatId);
                btn.setOnAction(this::selectSeat);
                break;

            default: // "available"
                switch (type) {
                    case "STANDARD":
                        btn.getStyleClass().add("seat-standard");
                        break;
                    case "VIP":
                        btn.getStyleClass().add("seat-vip");
                        break;
                    case "COUPLE":
                        btn.getStyleClass().add("seat-couple");
                        break;
                }
                btn.setOnAction(this::selectSeat);
                break;
        }
    }

    // ============================================================
    // FALLBACK: hardcoded seats (DB offline)
    // ============================================================
    private void buildSeatGridFallback() {
        seatGrid.getChildren().clear();
        seatGrid.setAlignment(Pos.CENTER);

        String[][] ROW_CONFIG = {
                { "A", "STANDARD", "1-10" }, { "B", "STANDARD", "1-10" },
                { "C", "STANDARD", "1-10" }, { "D", "STANDARD", "1-10" },
                { "E", "VIP", "1-10" }, { "F", "VIP", "1-10" }, { "G", "VIP", "1-10" },
                { "H", "COUPLE", "1-10" },
        };

        for (int rowIdx = 0; rowIdx < ROW_CONFIG.length; rowIdx++) {
            String rowLetter = ROW_CONFIG[rowIdx][0];
            String type = ROW_CONFIG[rowIdx][1];

            Label rowLabel = new Label(rowLetter);
            rowLabel.getStyleClass().add("seat-row-label");
            rowLabel.setMinWidth(28);
            rowLabel.setAlignment(Pos.CENTER);
            seatGrid.add(rowLabel, 0, rowIdx);

            int col = 1;
            for (int n = 1; n <= 10; n++) {
                if (n == 3 || n == 8) {
                    Region gap = new Region();
                    gap.setPrefWidth(16);
                    seatGrid.add(gap, col, rowIdx);
                    col++;
                }

                String seatNumber = rowLetter + n;
                int price = type.equals("VIP") ? 100000 : type.equals("COUPLE") ? 150000 : 50000;

                seatTypes.put(seatNumber, type);
                seatPrices.put(seatNumber, price);

                Button btn = new Button(String.valueOf(n));
                btn.getStyleClass().add("seat-btn");
                btn.setMinWidth(38);
                btn.setMinHeight(34);

                switch (type) {
                    case "STANDARD":
                        btn.getStyleClass().add("seat-standard");
                        break;
                    case "VIP":
                        btn.getStyleClass().add("seat-vip");
                        break;
                    case "COUPLE":
                        btn.getStyleClass().add("seat-couple");
                        break;
                }
                btn.setOnAction(this::selectSeat);

                seatGrid.add(btn, col, rowIdx);
                seatButtons.put(seatNumber, btn);
                col++;
            }
        }
    }

    // ============================================================
    // SEAT SELECTION with DB lock
    // ============================================================
    @FXML
    public void selectSeat(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String seatNumber = findSeatNumber(btn);
        if (seatNumber == null)
            return;
        String type = seatTypes.get(seatNumber);

        if (selectedSeats.contains(seatNumber)) {
            // --- DESELECT ---
            selectedSeats.remove(seatNumber);
            btn.getStyleClass().remove("seat-selected-orange");
            addTypeStyle(btn, type);

            // Unlock in DB
            Integer dbSeatId = BookingSession.getSeatIdByNumber(seatNumber);
            if (dbSeatId != null && seatLocksAvailable) {
                BookingSession.removeSeatId(dbSeatId);
                try {
                    enhancedBooking.unlockSeat(dbSeatId,
                            BookingSession.getShowtimeId(),
                            BookingSession.getSessionToken());
                } catch (Exception e) {
                    System.err.println("⚠ Unlock failed: " + e.getMessage());
                }
            }
        } else {
            // --- SELECT ---
            Integer dbSeatId = BookingSession.getSeatIdByNumber(seatNumber);

            // Try DB lock if seat_locks is available
            if (dbSeatId != null && seatLocksAvailable) {
                try {
                    int customerId = Session.getCurrentCustomer() != null
                            ? Session.getCurrentCustomer().getCustomerId()
                            : 0;
                    enhancedBooking.lockSeatsForBooking(
                            List.of(dbSeatId),
                            BookingSession.getShowtimeId(),
                            customerId,
                            BookingSession.getSessionToken());
                    BookingSession.addSeatId(dbSeatId);
                } catch (Exception e) {
                    showSeatUnavailable(seatNumber, btn);
                    return;
                }
            }

            // Also re-check seat status from DB to prevent double-booking
            if (dbSeatId != null && dbAvailable) {
                try {
                    SeatDAO seatDAO = new SeatDAO();
                    Seat freshSeat = seatDAO.findById(dbSeatId);
                    if (freshSeat != null && freshSeat.getStatus() != SeatStatus.AVAILABLE) {
                        showSeatUnavailable(seatNumber, btn);
                        // Undo the lock we just made
                        if (seatLocksAvailable) {
                            try {
                                enhancedBooking.unlockSeat(dbSeatId,
                                        BookingSession.getShowtimeId(),
                                        BookingSession.getSessionToken());
                            } catch (Exception ignored) {
                            }
                        }
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("⚠ Re-check seat status failed: " + e.getMessage());
                }
            }

            selectedSeats.add(seatNumber);
            btn.getStyleClass().removeAll("seat-standard", "seat-vip", "seat-couple");
            if (!btn.getStyleClass().contains("seat-selected-orange"))
                btn.getStyleClass().add("seat-selected-orange");
        }
        updateInfo();
    }

    private void showSeatUnavailable(String seatNumber, Button btn) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ghế không khả dụng");
        alert.setHeaderText(null);
        alert.setContentText(
                "Ghế " + seatNumber + " đã được đặt hoặc đang giữ bởi người khác.\nVui lòng chọn ghế khác.");
        alert.show();
        btn.getStyleClass().removeAll("seat-standard", "seat-vip", "seat-couple");
        btn.getStyleClass().add("seat-booked-dark");
        btn.setDisable(true);
        btn.setOpacity(0.35);
    }

    private void addTypeStyle(Button btn, String type) {
        switch (type) {
            case "STANDARD":
                if (!btn.getStyleClass().contains("seat-standard"))
                    btn.getStyleClass().add("seat-standard");
                break;
            case "VIP":
                if (!btn.getStyleClass().contains("seat-vip"))
                    btn.getStyleClass().add("seat-vip");
                break;
            case "COUPLE":
                if (!btn.getStyleClass().contains("seat-couple"))
                    btn.getStyleClass().add("seat-couple");
                break;
        }
    }

    private String findSeatNumber(Button btn) {
        for (Map.Entry<String, Button> e : seatButtons.entrySet()) {
            if (e.getValue() == btn)
                return e.getKey();
        }
        return null;
    }

    // ============================================================
    // INFO PANEL
    // ============================================================
    private void updateInfo() {
        totalPrice = 0;
        for (String seat : selectedSeats) {
            Integer p = seatPrices.get(seat);
            if (p != null)
                totalPrice += p;
        }

        if (totalPriceLabel != null)
            totalPriceLabel.setText(String.format("%,dđ", totalPrice).replace(',', '.'));

        if (countBadge != null) {
            int c = selectedSeats.size();
            countBadge.setText(c > 0 ? String.valueOf(c) : "");
            countBadge.setVisible(c > 0);
        }
        rebuildChips();
    }

    private void rebuildChips() {
        if (seatChipsPane == null)
            return;
        seatChipsPane.getChildren().clear();

        for (String seatNumber : selectedSeats) {
            Integer price = seatPrices.getOrDefault(seatNumber, 50000);
            String displayName = "Ghế " + seatNumber;

            VBox chip = new VBox(2);
            chip.getStyleClass().add("seat-chip");
            chip.setAlignment(Pos.CENTER_LEFT);

            HBox topRow = new HBox(6);
            topRow.setAlignment(Pos.CENTER_LEFT);
            Label nameLabel = new Label(displayName);
            nameLabel.getStyleClass().add("seat-chip-name");

            Button removeBtn = new Button("✕");
            removeBtn.getStyleClass().add("seat-chip-remove");
            removeBtn.setOnAction(e -> removeSeat(seatNumber));
            topRow.getChildren().addAll(nameLabel, removeBtn);

            Label priceLabel = new Label(String.format("%,dđ", price).replace(',', '.'));
            priceLabel.getStyleClass().add("seat-chip-price");

            chip.getChildren().addAll(topRow, priceLabel);
            seatChipsPane.getChildren().add(chip);
        }
    }

    private void removeSeat(String seatNumber) {
        if (!selectedSeats.contains(seatNumber))
            return;
        selectedSeats.remove(seatNumber);

        Button btn = seatButtons.get(seatNumber);
        if (btn != null) {
            btn.getStyleClass().remove("seat-selected-orange");
            addTypeStyle(btn, seatTypes.get(seatNumber));
        }

        Integer dbSeatId = BookingSession.getSeatIdByNumber(seatNumber);
        if (dbSeatId != null && seatLocksAvailable) {
            BookingSession.removeSeatId(dbSeatId);
            try {
                enhancedBooking.unlockSeat(dbSeatId,
                        BookingSession.getShowtimeId(),
                        BookingSession.getSessionToken());
            } catch (Exception e) {
                System.err.println("⚠ Unlock failed: " + e.getMessage());
            }
        }
        updateInfo();
    }

    // ============================================================
    // COUNTDOWN TIMER
    // ============================================================
    private void startCountdown() {
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            countdownSeconds--;
            if (countdownSeconds <= 0) {
                countdownTimeline.stop();
                countdownLabel.setText("00:00");
                if (seatLocksAvailable) {
                    try {
                        enhancedBooking.unlockAllSeats(
                                BookingSession.getSessionToken(),
                                BookingSession.getShowtimeId());
                    } catch (Exception ex) {
                        System.err.println("⚠ Unlock all failed: " + ex.getMessage());
                    }
                }
                showTimeoutAlert();
                return;
            }
            int min = countdownSeconds / 60;
            int sec = countdownSeconds % 60;
            countdownLabel.setText(String.format("%02d:%02d", min, sec));

            // Visual warning when < 2 min
            if (countdownSeconds < 120) {
                countdownLabel.setStyle("-fx-text-fill: #ff4444;");
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void showTimeoutAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Hết thời gian");
        alert.setHeaderText(null);
        alert.setContentText("Thời gian giữ ghế đã hết.\nVui lòng chọn lại ghế.");
        alert.showAndWait();
        goBackToHome();
    }

    // ============================================================
    // NAVIGATION
    // ============================================================
    @FXML
    public void togglePanel(ActionEvent event) {
        panelExpanded = !panelExpanded;
        seatChipsPane.setVisible(panelExpanded);
        seatChipsPane.setManaged(panelExpanded);
        togglePanelBtn.setText(panelExpanded ? "∧" : "∨");
    }

    @FXML
    public void goBack(ActionEvent event) {
        if (countdownTimeline != null)
            countdownTimeline.stop();
        if (seatLocksAvailable) {
            try {
                enhancedBooking.unlockAllSeats(
                        BookingSession.getSessionToken(),
                        BookingSession.getShowtimeId());
            } catch (Exception e) {
                System.err.println("⚠ Unlock all failed: " + e.getMessage());
            }
        }
        goBackToHome();
    }

    private void goBackToHome() {
        SceneManager.switchScene("home.fxml");
    }

    @FXML
    public void goSummary(ActionEvent event) {
        if (selectedSeats.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Chưa chọn ghế");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn ít nhất một ghế!");
            alert.show();
            return;
        }
        if (countdownTimeline != null)
            countdownTimeline.stop();
        SceneManager.switchScene("summary.fxml");
    }

    @FXML
    public void continueBooking(ActionEvent event) {
        goSummary(event);
    }

    @FXML
    public void continueBooking() {
        if (selectedSeats.isEmpty())
            return;
        if (countdownTimeline != null)
            countdownTimeline.stop();
        SceneManager.switchScene("summary.fxml");
    }
}
