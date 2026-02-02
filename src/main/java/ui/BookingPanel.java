package ui;

import javax.swing.*;
import java.awt.*;

public class BookingPanel extends JPanel {

    public BookingPanel() {
        setLayout(new GridLayout(6, 2, 10, 10));

        add(new JLabel("Customer Name:"));
        JTextField txtName = new JTextField();
        add(txtName);

        add(new JLabel("Phone:"));
        JTextField txtPhone = new JTextField();
        add(txtPhone);

        add(new JLabel("Payment Method:"));
        JComboBox<String> cbPayment = new JComboBox<>(
                new String[]{"CASH", "CARD"}
        );
        add(cbPayment);

        JButton btnBook = new JButton("Book Ticket");
        add(new JLabel());
        add(btnBook);

        btnBook.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Booking successful!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }
}
