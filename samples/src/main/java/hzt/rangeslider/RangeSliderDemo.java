package hzt.rangeslider;

import javax.swing.*;
import java.awt.*;

/**
 * Demo application panel to display a range slider.
 * <p>
 * Sources: <a href="https://github.com/ernieyu/Swing-range-slider">Github</a> <a href="https://ernienotes.wordpress.com/2010/12/27/creating-a-java-swing-range-slider/">Blog</a>
 */
public final class RangeSliderDemo {

    private final JLabel rangeSliderValue1 = new JLabel();
    private final JLabel rangeSliderValue2 = new JLabel();
    private final RangeSlider rangeSlider = new RangeSlider(Color.GREEN);

    public RangeSliderDemo() {
        rangeSliderValue1.setHorizontalAlignment(SwingConstants.LEFT);
        rangeSliderValue2.setHorizontalAlignment(SwingConstants.LEFT);

        rangeSlider.setPreferredSize(new Dimension(240, rangeSlider.getPreferredSize().height));
        rangeSlider.setMinimum(0);
        rangeSlider.setMaximum(10);

        // Add listener to update display.
        rangeSlider.addChangeListener(e -> {
            final var slider = (RangeSlider) e.getSource();
            rangeSliderValue1.setText(String.valueOf(slider.getValue()));
            rangeSliderValue2.setText(String.valueOf(slider.getUpperValue()));
        });
    }

    public void display() {
        final var rangeSliderLabel1 = new JLabel();
        rangeSliderLabel1.setText("Lower value:");
        final var rangeSliderLabel2 = new JLabel();
        rangeSliderLabel2.setText("Upper value:");

        final var panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.setLayout(new GridBagLayout());
        panel.add(rangeSliderLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 3, 3), 0, 0));
        panel.add(rangeSliderValue1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 3, 0), 0, 0));
        panel.add(rangeSliderLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 3, 3), 0, 0));
        panel.add(rangeSliderValue2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 6, 0), 0, 0));
        panel.add(rangeSlider, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        // Initialize values.
        rangeSlider.setValue(3);
        rangeSlider.setUpperValue(7);
        rangeSlider.setOrientation(SwingConstants.HORIZONTAL);

        // Initialize value display.
        rangeSliderValue1.setText(String.valueOf(rangeSlider.getValue()));
        rangeSliderValue2.setText(String.valueOf(rangeSlider.getUpperValue()));

        // Create window frame.
        final var frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setTitle("Range Slider Demo");

        // Set window content and validate.
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();

        // Set window location and display.
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Main application method.
     */
    void main() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(() -> new RangeSliderDemo().display());
    }
}
