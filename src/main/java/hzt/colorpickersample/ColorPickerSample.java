package hzt.colorpickersample;

import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class ColorPickerSample {

    public static void main(String[] args) {
        ColorPickerSample.run();
    }

    private static void run() {
        JFrame jFrame = new JFrame("ColorPicker sample");
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setContentPane(buildContent());
        jFrame.pack();
        jFrame.setVisible(true);
    }

    static JPanel buildContent() {
        JPanel contentPane = new JPanel();
        JColorChooser colorChooser = new JColorChooser();
        colorChooser.addPropertyChangeListener(evt -> contentPane.setBackground(colorChooser.getColor()));
        contentPane.add(colorChooser);
        contentPane.setOpaque(true);
        return contentPane;
    }
}
