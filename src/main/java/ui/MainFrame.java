package ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    CardLayout cardLayout;
    JPanel mainPanel;

    public MainFrame() {
        setTitle("Cinema Ticket Booking");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        HomePanel home = new HomePanel(this);
        MovieDetailPanel detail = new MovieDetailPanel(this);

        mainPanel.add(home, "HOME");
        mainPanel.add(detail, "DETAIL");

        add(mainPanel);
    }

    public void showDetail(String movieName) {
        MovieDetailPanel panel =
                (MovieDetailPanel) mainPanel.getComponent(1);
        panel.loadMovie(movieName);
        cardLayout.show(mainPanel, "DETAIL");
    }

    public void showHome() {
        cardLayout.show(mainPanel, "HOME");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new MainFrame().setVisible(true)
        );
    }
}
