package ui;

import model.MovieRating;
import util.IMDBApiClient;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class MovieAppDemo extends JFrame {

    private JTextField txtSearch;
    private JLabel lblTitle, lblYear, lblGenre, lblRating, lblPoster;
    private JTextArea txtPlot;

    public MovieAppDemo() {
        setTitle("🎬 Cinema Ticket Booking - Demo");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lbl = new JLabel("🎥 Movie Name:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

        txtSearch = new JTextField();
        JButton btnSearch = new JButton("Search");

        btnSearch.addActionListener(e -> searchMovie());

        panel.add(lbl, BorderLayout.WEST);
        panel.add(txtSearch, BorderLayout.CENTER);
        panel.add(btnSearch, BorderLayout.EAST);

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // LEFT: Poster
        lblPoster = new JLabel();
        lblPoster.setHorizontalAlignment(JLabel.CENTER);
        lblPoster.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // RIGHT: Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        lblTitle = new JLabel("Title: ");
        lblYear = new JLabel("Year: ");
        lblGenre = new JLabel("Genre: ");
        lblRating = new JLabel("IMDb Rating: ");

        txtPlot = new JTextArea(6, 20);
        txtPlot.setLineWrap(true);
        txtPlot.setWrapStyleWord(true);
        txtPlot.setEditable(false);

        JScrollPane scroll = new JScrollPane(txtPlot);

        Font font = new Font("Segoe UI", Font.PLAIN, 14);
        lblTitle.setFont(font);
        lblYear.setFont(font);
        lblGenre.setFont(font);
        lblRating.setFont(font);

        infoPanel.add(lblTitle);
        infoPanel.add(lblYear);
        infoPanel.add(lblGenre);
        infoPanel.add(lblRating);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(new JLabel("Plot:"));
        infoPanel.add(scroll);

        panel.add(lblPoster);
        panel.add(infoPanel);

        return panel;
    }

    private void searchMovie() {
        String name = txtSearch.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhap ten phim!");
            return;
        }

        MovieRating movie = IMDBApiClient.getMovieRating(name);

        if (movie == null) {
            JOptionPane.showMessageDialog(this, "Khong tim thay phim!");
            return;
        }

        lblTitle.setText("Title: " + movie.getTitle());
        lblYear.setText("Year: " + movie.getYear());
        lblGenre.setText("Genre: " + movie.getGenre());
        lblRating.setText("IMDb Rating: ⭐ " + movie.getImdbRating());
        txtPlot.setText(movie.getPlot());

        loadPoster(movie.getPoster());
    }

    private void loadPoster(String posterUrl) {
        try {
            if (posterUrl == null || posterUrl.equals("N/A")) {
                lblPoster.setIcon(null);
                return;
            }
            ImageIcon icon = new ImageIcon(new URL(posterUrl));
            Image img = icon.getImage().getScaledInstance(300, 450, Image.SCALE_SMOOTH);
            lblPoster.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblPoster.setIcon(null);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MovieAppDemo().setVisible(true));
    }
}
