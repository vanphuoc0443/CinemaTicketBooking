package dao;

import model.Movie;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {

    // Lay tat ca phim
    public List<Movie> findAll() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                movies.add(extractMovieFromResultSet(rs));
            }
        }

        return movies;
    }

    // Lay phim theo ID
    public Movie findById(int movieId) throws SQLException {
        String sql = "SELECT * FROM movies WHERE movie_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractMovieFromResultSet(rs);
                }
            }
        }

        return null;
    }

    // Tim kiem phim theo tu khoa
    public List<Movie> searchByKeyword(String keyword) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE title LIKE ? OR genre LIKE ? ORDER BY title";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movies.add(extractMovieFromResultSet(rs));
                }
            }
        }

        return movies;
    }

    // Them phim moi
    public boolean save(Movie movie) throws SQLException {
        String sql = "INSERT INTO movies (title, genre, duration, description, poster_url, release_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getGenre());
            stmt.setInt(3, movie.getDuration());
            stmt.setString(4, movie.getDescription());
            stmt.setString(5, movie.getPosterUrl());
            stmt.setDate(6, movie.getReleaseDate() != null ?
                    new java.sql.Date(movie.getReleaseDate().getTime()) : null);

            int result = stmt.executeUpdate();

            if (result > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        movie.setMovieId(rs.getInt(1));
                    }
                }
                conn.commit();
                return true;
            }
        }

        return false;
    }

    // Cap nhat phim
    public boolean update(Movie movie) throws SQLException {
        String sql = "UPDATE movies SET title = ?, genre = ?, duration = ?, " +
                "description = ?, poster_url = ?, release_date = ? WHERE movie_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getGenre());
            stmt.setInt(3, movie.getDuration());
            stmt.setString(4, movie.getDescription());
            stmt.setString(5, movie.getPosterUrl());
            stmt.setDate(6, movie.getReleaseDate() != null ?
                    new java.sql.Date(movie.getReleaseDate().getTime()) : null);
            stmt.setInt(7, movie.getMovieId());

            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    // Xoa phim
    public boolean delete(int movieId) throws SQLException {
        String sql = "DELETE FROM movies WHERE movie_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    // Trich xuat Movie tu ResultSet
    private Movie extractMovieFromResultSet(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setMovieId(rs.getInt("movie_id"));
        movie.setTitle(rs.getString("title"));
        movie.setGenre(rs.getString("genre"));
        movie.setDuration(rs.getInt("duration"));
        movie.setDescription(rs.getString("description"));
        movie.setPosterUrl(rs.getString("poster_url"));
        movie.setReleaseDate(rs.getDate("release_date"));
        movie.setCreatedAt(rs.getTimestamp("created_at"));
        movie.setUpdatedAt(rs.getTimestamp("updated_at"));
        return movie;
    }
}