package ui;

import model.MovieRating;
import util.IMDBApiClient;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class MovieDetailPanel extends JPanel {

    MainFrame frame;
    JLabel poster = new JLabel();
    JLabel title, rating, genre, year;
    JTextArea plot;

    public MovieDetailPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(15, 15));
        setBackground(Color.BLACK);

        JButton back = new JButton("← Back");
        back.addActionListener(e -> frame.showHome());

        title = label();
        rating = label();
        genre = label();
        year = label();

        plot = new JTextArea();
        plot.setLineWrap(true);
        plot.setWrapStyleWord(true);
        plot.setEditable(false);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(Color.BLACK);

        right.add(title);
        right.add(rating);
        right.add(genre);
        right.add(year);
        right.add(new JScrollPane(plot));

        JButton book = new JButton("ĐẶT VÉ");
        book.setBackground(Color.YELLOW);
        book.setFont(new Font("Segoe UI", Font.BOLD, 16));
        book.addActionListener(e -> new BookingDialog(frame).setVisible(true));

        right.add(book);

        add(back, BorderLayout.NORTH);
        add(poster, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
    }

    private JLabel label() {
        JLabel l = new JLabel();
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        return l;
    }

    public void loadMovie(String name) {
        MovieRating m = IMDBApiClient.getMovieRating(name);

        title.setText("🎬 " + m.getTitle());
        rating.setText("⭐ IMDb: " + m.getImdbRating());
        genre.setText("🎞️ Genre: " + m.getGenre());
        year.setText("📅 Year: " + m.getYear());
        plot.setText(m.getPlot());

        try {
            ImageIcon icon = new ImageIcon(new URL(m.getPoster()));
            Image img = icon.getImage().getScaledInstance(300, 450, Image.SCALE_SMOOTH);
            poster.setIcon(new ImageIcon(img));
        } catch (Exception ignored) {}
    }
}
