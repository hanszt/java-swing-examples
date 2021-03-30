package hzt.colorpickersample;

import javax.swing.*;

public class ColorPickerSample {

    public static void main(String[] args) {
        ColorPickerSample colorPickerSample = new ColorPickerSample();
        SwingUtilities.invokeLater(colorPickerSample::run);
    }

    private void run() {
        JFrame jFrame = new JFrame("ColorPicker sample");
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel contentPane = buildContent();
        contentPane.setOpaque(true);
        jFrame.setContentPane(contentPane);
        jFrame.pack();
        jFrame.setVisible(true);
    }

    private JPanel buildContent() {
        JPanel contentPane = new JPanel();
        JColorChooser colorChooser = new JColorChooser();
        contentPane.add(colorChooser);
        return contentPane;
    }
}
