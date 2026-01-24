package util;

import model.MovieRating;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// Class ket noi OMDB API (IMDB data)
public class IMDBApiClient {

    // Dang ky tai https://www.omdbapi.com/apikey.aspx
    private static final String API_KEY = "YOUR_API_KEY_HERE";
    private static final String BASE_URL = "api.imdbapi.dev";

    // Lay thong tin phim theo ten
    public static MovieRating getMovieRating(String movieTitle) {
        try {
            String urlString = BASE_URL + "?apikey=" + API_KEY + "&t=" +
                    movieTitle.replace(" ", "+");

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return parseMovieRating(response.toString());
            }

        } catch (Exception e) {
            System.err.println("Loi khi lay rating: " + e.getMessage());
        }

        return null;
    }

    // Parse JSON response
    private static MovieRating parseMovieRating(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);

            if (json.getString("Response").equals("False")) {
                return null;
            }

            MovieRating rating = new MovieRating();
            rating.setTitle(json.optString("Title", "N/A"));
            rating.setYear(json.optString("Year", "N/A"));
            rating.setRated(json.optString("Rated", "N/A"));
            rating.setReleased(json.optString("Released", "N/A"));
            rating.setRuntime(json.optString("Runtime", "N/A"));
            rating.setGenre(json.optString("Genre", "N/A"));
            rating.setDirector(json.optString("Director", "N/A"));
            rating.setActors(json.optString("Actors", "N/A"));
            rating.setPlot(json.optString("Plot", "N/A"));
            rating.setPoster(json.optString("Poster", "N/A"));
            rating.setImdbRating(json.optString("imdbRating", "N/A"));
            rating.setImdbVotes(json.optString("imdbVotes", "N/A"));
            rating.setImdbID(json.optString("imdbID", "N/A"));

            return rating;

        } catch (Exception e) {
            System.err.println("Loi parse JSON: " + e.getMessage());
        }

        return null;
    }

    // Test API
    public static void main(String[] args) {
        System.out.println("Testing IMDB API...\n");

        MovieRating rating = getMovieRating("Avatar");

        if (rating != null) {
            System.out.println("Title: " + rating.getTitle());
            System.out.println("Year: " + rating.getYear());
            System.out.println("Genre: " + rating.getGenre());
            System.out.println("IMDB Rating: " + rating.getImdbRating());
            System.out.println("IMDB Votes: " + rating.getImdbVotes());
            System.out.println("Plot: " + rating.getPlot());
        } else {
            System.out.println("Khong tim thay phim");
        }
    }
}