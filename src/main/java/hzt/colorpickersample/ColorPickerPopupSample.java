package hzt.colorpickersample;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;

public class ColorPickerPopupSample {

    public static void main(String[] args) {
        JFrame frame = new JFrame("JColorChooser Sample Popup");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final JButton button = new JButton("Pick to Change Background");

        button.addActionListener(actionEvent -> changeBackgroundColor(button));
        frame.add(button, BorderLayout.CENTER);

        frame.setSize(300, 100);
        frame.setVisible(true);
    }

    private static void changeBackgroundColor(JButton button) {
        Color initialBackground = button.getBackground();
        Color background = JColorChooser.showDialog(null, "Change Button Background", initialBackground);
        if (background != null) {
            button.setBackground(background);
        }
    }
}
