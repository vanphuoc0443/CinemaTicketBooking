package service;

import model.Movie;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Service class for fetching movie data from the OMDB API.
 * Register for a free key at: https://www.omdbapi.com/apikey.aspx
 */
public class OmdbApiService {

    // Replace with your own OMDB API key
    private static final String API_KEY = "b6003d8a";
    private static final String BASE_URL = "https://www.omdbapi.com/";

    /**
     * Search movies by title keyword.
     * Returns a list of movies with basic info (title, year, poster, imdbId).
     */
    public List<Movie> searchMovies(String query) {
        List<Movie> movies = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String urlStr = BASE_URL + "?s=" + encoded + "&type=movie&apikey=" + API_KEY;
            String json = httpGet(urlStr);

            JSONObject response = new JSONObject(json);
            if (response.optString("Response", "False").equals("True")) {
                JSONArray results = response.getJSONArray("Search");
                int limit = Math.min(results.length(), 8); // Limit to 8 results
                for (int i = 0; i < limit; i++) {
                    JSONObject item = results.getJSONObject(i);
                    Movie movie = new Movie();
                    movie.setTitle(item.optString("Title", "Unknown"));
                    movie.setPosterUrl(item.optString("Poster", "N/A"));
                    movie.setImdbId(item.optString("imdbID", ""));

                    // Extract year and set as genre placeholder until details are fetched
                    String year = item.optString("Year", "");
                    movie.setGenre(year);

                    movies.add(movie);
                }
            }
        } catch (Exception e) {
            System.err.println("OMDB search error: " + e.getMessage());
            e.printStackTrace();
        }
        return movies;
    }

    /**
     * Get full movie details by IMDb ID.
     * Returns a Movie with title, genre, duration, description, rating, poster,
     * director, language, release date.
     */
    public Movie getMovieDetails(String imdbId) {
        try {
            String urlStr = BASE_URL + "?i=" + imdbId + "&plot=full&apikey=" + API_KEY;
            String json = httpGet(urlStr);

            JSONObject item = new JSONObject(json);
            if (item.optString("Response", "False").equals("True")) {
                Movie movie = new Movie();
                movie.setTitle(item.optString("Title", "Unknown"));
                movie.setGenre(item.optString("Genre", "N/A"));
                movie.setDescription(item.optString("Plot", "No description available."));
                movie.setPosterUrl(item.optString("Poster", "N/A"));
                movie.setImdbRating(item.optString("imdbRating", "N/A"));
                movie.setImdbId(imdbId);
                movie.setDirector(item.optString("Director", "N/A"));
                movie.setLanguage(item.optString("Language", "N/A"));

                // Parse Released date (e.g. "01 May 2012")
                String released = item.optString("Released", "N/A");
                if (released != null && !released.equals("N/A")) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
                        Date releaseDate = sdf.parse(released);
                        movie.setReleaseDate(releaseDate);
                    } catch (Exception dateEx) {
                        // Ignore parse errors
                    }
                }

                // Parse runtime (e.g. "148 min" -> 148)
                String runtime = item.optString("Runtime", "0 min");
                try {
                    movie.setDuration(Integer.parseInt(runtime.replaceAll("[^0-9]", "")));
                } catch (NumberFormatException e) {
                    movie.setDuration(0);
                }

                return movie;
            }
        } catch (Exception e) {
            System.err.println("OMDB details error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Perform an HTTP GET request and return the response body as a String.
     */
    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("OMDB API returned HTTP " + code);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }
}
