package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MoviePanel extends JPanel {

    public MoviePanel() {
        setLayout(new BorderLayout());

        String[] columns = {"ID", "Title", "Duration", "Rating"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton btnLoad = new JButton("Load Movies");

        btnLoad.addActionListener(e -> {
            model.setRowCount(0);
            model.addRow(new Object[]{1, "Avengers", 120, "PG-13"});
            model.addRow(new Object[]{2, "Inception", 148, "PG-13"});
        });

        add(scrollPane, BorderLayout.CENTER);
        add(btnLoad, BorderLayout.SOUTH);
    }
}
