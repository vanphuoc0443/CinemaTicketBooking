package ui;

import javax.swing.*;
import java.awt.*;

public class BookingDialog extends JDialog {

    public BookingDialog(JFrame parent) {
        super(parent, "Đặt vé", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Rạp:"));
        add(new JComboBox<>(new String[]{"CGV", "BETA"}));

        add(new JLabel("Suất chiếu:"));
        add(new JComboBox<>(new String[]{"14:00", "17:30", "20:00"}));

        add(new JLabel("Ghế:"));
        add(new JComboBox<>(new String[]{"A1", "A2", "B3"}));

        JButton ok = new JButton("XÁC NHẬN");
        ok.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Đặt vé thành công!");
            dispose();
        });

        add(new JLabel());
        add(ok);
    }
}
