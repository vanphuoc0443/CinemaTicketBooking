package ui;

import javax.swing.*;
import java.awt.*;

public class ShowtimePanel extends JPanel {

    public ShowtimePanel() {
        setLayout(new GridLayout(5, 2, 10, 10));

        add(new JLabel("Movie:"));
        JComboBox<String> cbMovie = new JComboBox<>(new String[]{
                "Avengers", "Inception"
        });
        add(cbMovie);

        add(new JLabel("Date:"));
        JTextField txtDate = new JTextField("2026-01-25");
        add(txtDate);

        add(new JLabel("Time:"));
        JTextField txtTime = new JTextField("19:30");
        add(txtTime);

        JButton btnLoad = new JButton("Load Showtimes");
        add(new JLabel());
        add(btnLoad);
    }
}
