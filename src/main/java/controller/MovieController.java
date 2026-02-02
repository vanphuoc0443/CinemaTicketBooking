package controller;

import dao.MovieDAO;
import model.Movie;
import model.MovieRating;
import util.IMDBApiClient;
import exception.DatabaseException;
import java.sql.SQLException;
import java.util.List;

public class MovieController {
    private MovieDAO movieDAO;

    public MovieController(MovieDAO movieDAO) {
        this.movieDAO = movieDAO;
    }

    public List<Movie> getAllMovies() throws DatabaseException {
        try {
            return movieDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Khong the tai danh sach phim", e);
        }
    }

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

    // LAY RATING TU IMDB
    public MovieRating getIMDBRating(String movieTitle) {
        return IMDBApiClient.getMovieRating(movieTitle);
    }

    // LAY RATING TU IMDB CHO MOVIE OBJECT
    public MovieRating getIMDBRating(Movie movie) {
        if (movie == null || movie.getTitle() == null) {
            return null;
        }
        return IMDBApiClient.getMovieRating(movie.getTitle());
    }

    public boolean addMovie(Movie movie) throws DatabaseException {
        try {
            validateMovie(movie);
            return movieDAO.save(movie);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the them phim", e);
        }
    }

    public boolean updateMovie(Movie movie) throws DatabaseException {
        try {
            validateMovie(movie);
            return movieDAO.update(movie);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the cap nhat phim", e);
        }
    }

    public boolean deleteMovie(int movieId) throws DatabaseException {
        try {
            return movieDAO.delete(movieId);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the xoa phim", e);
        }
    }

    private void validateMovie(Movie movie) throws DatabaseException {
        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            throw new DatabaseException("Ten phim khong duoc de trong");
        }
        if (movie.getDuration() <= 0) {
            throw new DatabaseException("Thoi luong phim phai lon hon 0");
        }
    }
}
