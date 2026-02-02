package ui;

import javax.swing.*;
import java.awt.*;

public class HomePanel extends JPanel {

    MainFrame frame;

    public HomePanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        JLabel title = new JLabel("PHIM ĐANG CHIẾU");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

        JPanel movieGrid = new JPanel(new GridLayout(1, 4, 20, 20));
        movieGrid.setBackground(Color.BLACK);
        movieGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        movieGrid.add(movieButton("Avatar"));
        movieGrid.add(movieButton("Inception"));
        movieGrid.add(movieButton("Interstellar"));
        movieGrid.add(movieButton("Titanic"));

        add(title, BorderLayout.NORTH);
        add(movieGrid, BorderLayout.CENTER);
    }

    private JButton movieButton(String name) {
        JButton btn = new JButton("<html><center>" + name + "</center></html>");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(40, 40, 40));
        btn.setFocusPainted(false);

        btn.addActionListener(e -> frame.showDetail(name));
        return btn;
    }
}
