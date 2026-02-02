package util.test;

public class ImdbApiTest {
    public static void main(String[] args) {
        System.out.println(ImdbApiService.getMovieRating("Avatar"));
        System.out.println(ImdbApiService.getMovieRating("The Godfather"));
        System.out.println(ImdbApiService.getMovieRating("Spider-Man: No Way Home"));
        System.out.println(ImdbApiService.getMovieRating("xyzabc123notexist"));
    }
}
