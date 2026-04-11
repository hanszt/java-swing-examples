package hzt.samples3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * See <a href="https://www.youtube.com/watch?v=d4EgbgTm0Bg">Visualizing quaternions (4d numbers) with stereographic projection</a>
 * See <a href="http://blog.rogach.org/2015/08/how-to-create-your-own-simple-3d-render.html">How to create your own simple 3D render engine in pure Java</a>
 */
public final class Demo3DQuaternions {

    private static final Logger logger = LoggerFactory.getLogger(Demo3DQuaternions.class);

    void main() {
        final var resetButton = new JButton("Reset");
        final var modeButton = new RenderingModeButton(newMode -> logger.info("Switching to mode {}", newMode));

        final var headingSlider = createSlider(-180, 180, 0);
        final var pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        final var zoomSlider = createSlider(1, 1_000, 200);
        final var shadingSlider = createSlider(0, 1_000, 200);
        final var inflationSlider = createSlider(1, 8, 5, 1);

        final var renderPanel = new Demo3DPanel(headingSlider, pitchSlider, zoomSlider, shadingSlider, inflationSlider, modeButton);

        final var sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.add(headingSlider, BorderLayout.NORTH);
        sliderPanel.add(zoomSlider, BorderLayout.CENTER);
        sliderPanel.add(shadingSlider, BorderLayout.SOUTH);

        final var buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(modeButton);
        buttonPanel.add(resetButton);

        final var bottomControlPanel = new JPanel(new BorderLayout());
        bottomControlPanel.add(sliderPanel, BorderLayout.NORTH);
        bottomControlPanel.add(inflationSlider, BorderLayout.CENTER);
        bottomControlPanel.add(buttonPanel, BorderLayout.SOUTH);

        headingSlider.addChangeListener(_ -> renderPanel.repaint());
        pitchSlider.addChangeListener(_ -> renderPanel.repaint());
        zoomSlider.addChangeListener(_ -> renderPanel.repaint());
        inflationSlider.addChangeListener(_ -> renderPanel.updateShape());
        shadingSlider.addChangeListener(_ -> renderPanel.repaint());
        modeButton.addActionListener(_ -> {
            shadingSlider.setVisible(modeButton.getMode() == RenderingModeButton.Mode.FILLED_SHADED);
            renderPanel.repaint();
        });
        resetButton.addActionListener(_ -> {
            headingSlider.setValue(0);
            pitchSlider.setValue(0);
            zoomSlider.setValue(200);
            shadingSlider.setValue(200);
            inflationSlider.setValue(5);
            renderPanel.reset();
        });
        final var frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        final var pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());
        pane.add(bottomControlPanel, BorderLayout.SOUTH);
        pane.add(pitchSlider, BorderLayout.EAST);
        pane.add(renderPanel, BorderLayout.CENTER);
        frame.setTitle("3D Quaternions");
        frame.setSize(600, 600);
        frame.setVisible(true);
    }

    private JSlider createSlider(int min, int max, int value) {
        return createSlider(min, max, value, 0);
    }

    private JSlider createSlider(int min, int max, int value, int minorTickSpacing) {
        final var slider = new JSlider(min, max, value);
        if (minorTickSpacing > 0) {
            slider.setMinorTickSpacing(minorTickSpacing);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
        }
        return slider;
    }
}
