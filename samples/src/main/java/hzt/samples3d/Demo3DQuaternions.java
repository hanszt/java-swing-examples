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
        final var modeButton = new RenderingModeButton(newMode -> logger.info("Switching to mode {}", newMode));
        // slider to control horizontal rotation
        final var headingSlider = new JSlider(-180, 180, 0);
        // slider to control vertical rotation
        final var pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        final var zoomSlider = new JSlider(1, 1_000, 200);
        final var shadingSlider = new JSlider(0, 1_000, 200);

        // slider to control inflation
        final var inflationSlider = new JSlider(1, 8, 5);
        inflationSlider.setMinorTickSpacing(1);
        inflationSlider.setPaintTicks(true);
        inflationSlider.setPaintLabels(true);

        final var sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.add(headingSlider, BorderLayout.NORTH);
        sliderPanel.add(zoomSlider, BorderLayout.CENTER);
        sliderPanel.add(shadingSlider, BorderLayout.SOUTH);
        final var bottomControlPanel = new JPanel(new BorderLayout());

        bottomControlPanel.add(sliderPanel, BorderLayout.NORTH);
        bottomControlPanel.add(inflationSlider, BorderLayout.CENTER);
        bottomControlPanel.add(modeButton, BorderLayout.SOUTH);

        final var renderPanel = new Demo3DPanel(headingSlider, pitchSlider, zoomSlider, shadingSlider, inflationSlider, modeButton);

        headingSlider.addChangeListener(_ -> renderPanel.repaint());
        pitchSlider.addChangeListener(_ -> renderPanel.repaint());
        zoomSlider.addChangeListener(_ -> renderPanel.repaint());
        inflationSlider.addChangeListener(_ -> renderPanel.updateShape());
        shadingSlider.addChangeListener(_ -> renderPanel.repaint());
        modeButton.addActionListener(_ -> {
            shadingSlider.setVisible(modeButton.getMode() == RenderingModeButton.Mode.FILLED_SHADED);
            renderPanel.repaint();
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
}
