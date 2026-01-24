package controller;

import dao.MovieDAO;
import model.Movie;
import exception.DatabaseException;
import java.sql.SQLException;
import java.util.List;

public class MovieController {
    private MovieDAO movieDAO;

    public MovieController(MovieDAO movieDAO) {
        this.movieDAO = movieDAO;
    }

    // Lay tat ca phim
    public List<Movie> getAllMovies() throws DatabaseException {
        try {
            return movieDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Khong the tai danh sach phim", e);
        }
    }

    // Lay phim theo ID
    public Movie getMovieById(int movieId) throws DatabaseException {
        try {
            Movie movie = movieDAO.findById(movieId);
            if (movie == null) {
                throw new DatabaseException("Khong tim thay phim voi ID: " + movieId);
            }
            return movie;
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi tim phim", e);
        }
    }

    // Tim kiem phim theo tu khoa
    public List<Movie> searchMovies(String keyword) throws DatabaseException {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllMovies();
            }
            return movieDAO.searchByKeyword(keyword.trim());
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi tim kiem phim", e);
        }
    }

    // Them phim moi
    public boolean addMovie(Movie movie) throws DatabaseException {
        try {
            validateMovie(movie);
            return movieDAO.save(movie);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the them phim", e);
        }
    }

    // Cap nhat thong tin phim
    public boolean updateMovie(Movie movie) throws DatabaseException {
        try {
            validateMovie(movie);
            return movieDAO.update(movie);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the cap nhat phim", e);
        }
    }

    // Xoa phim
    public boolean deleteMovie(int movieId) throws DatabaseException {
        try {
            return movieDAO.delete(movieId);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the xoa phim", e);
        }
    }

    // Validate thong tin phim
    private void validateMovie(Movie movie) throws DatabaseException {
        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            throw new DatabaseException("Ten phim khong duoc de trong");
        }
        if (movie.getDuration() <= 0) {
            throw new DatabaseException("Thoi luong phim phai lon hon 0");
        }
    }
}