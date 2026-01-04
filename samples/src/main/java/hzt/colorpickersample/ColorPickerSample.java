package hzt.colorpickersample;

import javax.swing.*;

public class ColorPickerSample {

    public static void main() {
        ColorPickerSample.run();
    }

    private static void run() {
        final var jFrame = new JFrame("ColorPicker sample");
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setContentPane(buildContent());
        jFrame.pack();
        jFrame.setVisible(true);
    }

    static JPanel buildContent() {
        final var contentPane = new JPanel();
        final var colorChooser = new JColorChooser();
        colorChooser.addPropertyChangeListener(evt -> contentPane.setBackground(colorChooser.getColor()));
        contentPane.add(colorChooser);
        contentPane.setOpaque(true);
        return contentPane;
    }
}
