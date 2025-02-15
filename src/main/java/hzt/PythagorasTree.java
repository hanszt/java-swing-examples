package hzt;

import org.hzt.swing_utils.builders.JSliderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.geom.Path2D;

public final class PythagorasTree extends JPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(PythagorasTree.class);

    private static final int DEPTH_LIMIT = 16;
    private static final int INIT_ANGLE_1 = 2;
    private static final int INIT_ANGLE_2 = 2;
    private static final int INIT_DEPTH = 10;
    private static final int INIT_HUE = 50;
    private static final float INIT_SQUARE_SIDE_LENGTH = 150;
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 700;

    private final JSlider treeDepthSilder = JSliderBuilder.buildSlider()
            .withName("tree depth slider")
            .withInitValue(INIT_DEPTH)
            .withMinimum(1)
            .withMaximum(DEPTH_LIMIT)
            .withPaintLabels(true)
            .withPaintTicks(true)
            .build();
    private final JSlider hueSlider = JSliderBuilder.buildSlider()
            .withName("Hue slider")
            .withInitValue(INIT_HUE)
            .withMinimum(0)
            .withMaximum(360)
            .build();
    private final JSlider angleSlider1 = JSliderBuilder.buildSlider()
            .withName("angle slider 1")
            .withInitValue(INIT_ANGLE_2)
            .withMinimum(1)
            .withMaximum(10)
            .build();
    private final JSlider angleSlider2 = JSliderBuilder.buildSlider()
            .withName("angle slider 2")
            .withInitValue(INIT_ANGLE_2)
            .withMinimum(1)
            .withMaximum(10)
            .build();

    PythagorasTree() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.white);
    }

    /**
     * Initial base of tree
     * <p>
     * x1, y1----------x2, y2
     */
    private void drawTree(final Graphics graphics) {
        final var X1 = (WINDOW_WIDTH / 2F) - (INIT_SQUARE_SIDE_LENGTH / 2F);
        final var X2 = X1 + INIT_SQUARE_SIDE_LENGTH;
        final var Y = WINDOW_HEIGHT - 20;
        drawTree((Graphics2D) graphics, X1, Y, X2, Y, 0, new Counter());
    }

    private void drawTree(
            final Graphics2D graphics2D,
            final float x1,
            final float y1,
            final float x2,
            final float y2,
            final int depth,
            final Counter counter
    ) {
        if (depth == treeDepthSilder.getValue()) {
            counter.count++;
            final var count = counter.count;
            if (count % Math.pow(2, depth - 4) == 0) {
                LOGGER.atInfo()
                        .setMessage(() -> "Leaf triangle Side %-5d: from x = %4.2f, y = %4.2f to x: %4.2f, y: %4.2f".formatted(count, x1, y1, x2, y2))
                        .log();
            }
            return;
        }

        final var dx = x2 - x1;
        final var dy = y1 - y2;

        final var x3 = x2 - dy;
        final var y3 = y2 - dx;
        final var x4 = x1 - dy;
        final var y4 = y1 - dx;

        final Path2D square = new Path2D.Float();
        square.moveTo(x1, y1);
        square.lineTo(x2, y2);
        square.lineTo(x3, y3);
        square.lineTo(x4, y4);
        square.closePath();

        final var hue = hueSlider.getValue() / 360.0F;
        final var hsbColor1 = Color.getHSBColor(hue + depth * 0.02F, 1, 1);
        graphics2D.setColor(hsbColor1);
        graphics2D.fill(square);
        graphics2D.setColor(hsbColor1.darker());
        graphics2D.draw(square);

        final var x5 = x4 + (dx - dy) / angleSlider1.getValue();
        final var y5 = y4 - (dx + dy) / angleSlider2.getValue();
        final Path2D triangle = new Path2D.Float();
        triangle.moveTo(x3, y3);
        triangle.lineTo(x4, y4);
        triangle.lineTo(x5, y5);
        triangle.closePath();

        final var hsbColor = Color.getHSBColor(hue + depth * 0.035F, 1, 1);
        graphics2D.setColor(hsbColor);
        graphics2D.fill(triangle);
        graphics2D.setColor(hsbColor.darker());
        graphics2D.draw(triangle);

        drawTree(graphics2D, x4, y4, x5, y5, depth + 1, counter);
        drawTree(graphics2D, x5, y5, x3, y3, depth + 1, counter);
    }

    private void redraw() {
        LOGGER.info("Going to redraw...");
        paintComponent(getGraphics());
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        drawTree(g);
    }

    public void main() {
        SwingUtilities.invokeLater(this::run);
    }

    private void run() {
        final var controlPanel = new JPanel();
        final var resetAll = new JButton("Reset all");
        resetAll.addActionListener(_ -> {
            treeDepthSilder.setValue(INIT_DEPTH);
            angleSlider1.setValue(INIT_ANGLE_1);
            angleSlider2.setValue(INIT_ANGLE_2);
            hueSlider.setValue(INIT_HUE);
        });
        controlPanel.add(resetAll);
        controlPanel.add(new ResettableSlider(hueSlider));
        controlPanel.add(new ResettableSlider(angleSlider1));
        controlPanel.add(new ResettableSlider(angleSlider2));
        controlPanel.add(new ResettableSlider(treeDepthSilder));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        angleSlider1.addChangeListener(this::drawTreeAfterChange);
        angleSlider2.addChangeListener(this::drawTreeAfterChange);
        treeDepthSilder.addChangeListener(this::drawTreeAfterChange);
        hueSlider.addChangeListener(this::drawTreeAfterChange);

        configureSlider(angleSlider1, 1);
        configureSlider(angleSlider2, 1);
        configureSlider(treeDepthSilder, 2);
        configureSlider(hueSlider, 60);

        final var frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Pythagoras Tree");
//        frame.setResizable(false);
        frame.add(this, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.PAGE_END);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void configureSlider(final JSlider slider, final int majorTickSpacing) {
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(majorTickSpacing);
        slider.setMinorTickSpacing(1);
        slider.setSnapToTicks(true);
    }

    private void drawTreeAfterChange(final ChangeEvent e) {
        final var slider = (JSlider) e.getSource();
        if (!slider.getValueIsAdjusting()) {
            redraw();
        }
    }

    private static class Counter {
        private long count = 0;
    }

    private static class ResettableSlider extends JPanel {

        private ResettableSlider(final JSlider slider) {
            super();
            final var initValue = slider.getValue();
            final var resetButton = new JButton("Reset");
            resetButton.addActionListener(_ -> slider.setValue(initValue));
            add(slider);
            add(resetButton);
        }
    }
}
