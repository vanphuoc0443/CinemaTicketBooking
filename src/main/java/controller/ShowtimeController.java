package controller;

import dao.ShowtimeDAO;
import dao.MovieDAO;
import dao.BookingDAO;
import dao.StoredProcedureDAO;
import model.Showtime;
import model.Movie;
import model.Booking;
import exception.DatabaseException;
import util.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ShowtimeController {
    private ShowtimeDAO showtimeDAO;
    private MovieDAO movieDAO;

    public ShowtimeController(ShowtimeDAO showtimeDAO, MovieDAO movieDAO) {
        this.showtimeDAO = showtimeDAO;
        this.movieDAO = movieDAO;
    }

    // Lay tat ca suat chieu
    public List<Showtime> getAllShowtimes() throws DatabaseException {
        try {
            return showtimeDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Khong the tai danh sach suat chieu", e);
        }
    }

    // Lay suat chieu theo phim
    public List<Showtime> getShowtimesByMovie(int movieId) throws DatabaseException {
        try {
            Movie movie = movieDAO.findById(movieId);
            if (movie == null) {
                throw new DatabaseException("Phim khong ton tai");
            }
            return showtimeDAO.findByMovieId(movieId);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the tai suat chieu", e);
        }
    }

    // Lay suat chieu theo ngay
    public List<Showtime> getShowtimesByDate(String date) throws DatabaseException {
        try {
            validateDate(date);
            return showtimeDAO.findByDate(date);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the tai suat chieu", e);
        }
    }

    // Lay chi tiet suat chieu
    public Showtime getShowtimeDetails(int showtimeId) throws DatabaseException {
        try {
            Showtime showtime = showtimeDAO.findById(showtimeId);
            if (showtime == null) {
                throw new DatabaseException("Khong tim thay suat chieu");
            }
            return showtime;
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi tai thong tin suat chieu", e);
        }
    }

    // Them suat chieu moi
    public boolean addShowtime(Showtime showtime) throws DatabaseException {
        Connection conn = null;
        try {
            validateShowtime(showtime);

            // Kiem tra xung dot phong chieu
            if (hasRoomConflict(showtime)) {
                throw new DatabaseException("Phong chieu da co suat chieu vao thoi gian nay");
            }

            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Luu suat chieu
            boolean saved = showtimeDAO.save(showtime);

            if (saved && showtime.getShowtimeId() > 0) {
                // Tao 80 ghe tu dong
                StoredProcedureDAO.createSeatsForShowtime(conn, showtime.getShowtimeId());
                conn.commit();
                return true;
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new DatabaseException("Khong the them suat chieu", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Cap nhat suat chieu
    public boolean updateShowtime(Showtime showtime) throws DatabaseException {
        try {
            validateShowtime(showtime);

            // Kiem tra co ve da dat chua
            if (hasBookings(showtime.getShowtimeId())) {
                throw new DatabaseException("Khong the cap nhat suat chieu da co nguoi dat ve");
            }

            return showtimeDAO.update(showtime);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the cap nhat suat chieu", e);
        }
    }

    // Xoa suat chieu
    public boolean deleteShowtime(int showtimeId) throws DatabaseException {
        try {
            // Kiem tra co ve da dat chua
            if (hasBookings(showtimeId)) {
                throw new DatabaseException("Khong the xoa suat chieu da co nguoi dat ve");
            }

            return showtimeDAO.delete(showtimeId);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the xoa suat chieu", e);
        }
    }

    // Validate thong tin suat chieu
    private void validateShowtime(Showtime showtime) throws DatabaseException {
        if (showtime.getMovieId() <= 0) {
            throw new DatabaseException("ID phim khong hop le");
        }
        if (showtime.getShowDate() == null || showtime.getShowDate().isEmpty()) {
            throw new DatabaseException("Ngay chieu khong duoc de trong");
        }
        if (showtime.getShowTime() == null || showtime.getShowTime().isEmpty()) {
            throw new DatabaseException("Gio chieu khong duoc de trong");
        }
        if (showtime.getRoomNumber() <= 0) {
            throw new DatabaseException("So phong khong hop le");
        }

        // Kiem tra ngay chieu >= hom nay
        validateDate(showtime.getShowDate());
    }

    // Validate dinh dang ngay
    private void validateDate(String date) throws DatabaseException {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new DatabaseException("Dinh dang ngay khong hop le (yyyy-MM-dd)");
        }

        try {
            LocalDate showDate = LocalDate.parse(date);
            LocalDate today = LocalDate.now();

            if (showDate.isBefore(today)) {
                throw new DatabaseException("Ngay chieu phai >= hom nay");
            }
        } catch (Exception e) {
            throw new DatabaseException("Ngay khong hop le");
        }
    }

    // Kiem tra xung dot phong chieu
    private boolean hasRoomConflict(Showtime showtime) throws SQLException {
        return showtimeDAO.checkRoomConflict(
                showtime.getRoomNumber(),
                showtime.getShowDate(),
                showtime.getShowTime()
        );
    }

    // Kiem tra suat chieu co ve da dat
    private boolean hasBookings(int showtimeId) throws SQLException {
        BookingDAO bookingDAO = new BookingDAO();
        List<Booking> bookings = bookingDAO.findByShowtime(showtimeId);
        return !bookings.isEmpty();
    }
}