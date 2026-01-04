package hzt.colorpickersample;

import javax.swing.*;
import java.awt.*;

public class ColorPickerPopupSample {

    static void main() {
        final var frame = new JFrame("JColorChooser Sample Popup");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final var button = new JButton("Pick to Change Background");

        button.addActionListener(actionEvent -> changeBackgroundColor(button));
        frame.add(button, BorderLayout.CENTER);

        frame.setSize(300, 100);
        frame.setVisible(true);
    }

    private static void changeBackgroundColor(final JButton button) {
        final var initialBackground = button.getBackground();
        final var background = JColorChooser.showDialog(null, "Change Button Background", initialBackground);
        if (background != null) {
            button.setBackground(background);
        }
    }
}
