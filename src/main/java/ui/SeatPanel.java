package ui;

import javax.swing.*;
import java.awt.*;

public class SeatPanel extends JPanel {

    public SeatPanel() {
        setLayout(new GridLayout(5, 5, 5, 5));

        for (int i = 1; i <= 25; i++) {
            JToggleButton seat = new JToggleButton("S" + i);
            add(seat);
        }
    }
}
