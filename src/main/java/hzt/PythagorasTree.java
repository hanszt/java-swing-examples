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
    private static final int DEPTH_LIMIT = 15;
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 700;
    private static final float INIT_SQUARE_SIDE_LENGTH = 150;

    private final JSlider treeDepthSilder = JSliderBuilder.buildSlider()
            .withName("tree depth slider")
            .withInitValue(10)
            .withMinimum(1)
            .withMaximum(DEPTH_LIMIT)
            .withPaintLabels(true)
            .withPaintTicks(true)
            .build();
    private final JSlider angleSlider1 = JSliderBuilder.buildSlider()
            .withName("angle slider 1")
            .withInitValue(2)
            .withMinimum(1)
            .withMaximum(10)
            .build();
    private final JSlider angleSlider2 = JSliderBuilder.buildSlider()
            .withName("angle slider 2")
            .withInitValue(2)
            .withMinimum(1)
            .withMaximum(10)
            .build();

    private PythagorasTree() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.white);
    }

    private void drawTree(final Graphics2D graphics2D, final float x1, final float y1, final float x2, final float y2, final int depth) {
        if (depth == treeDepthSilder.getValue()) {
            return;
        }

        final var dx = x2 - x1;
        final var dy = y1 - y2;

        final var x3 = x2 - dy;
        final var y3 = y2 - dx;
        final var x4 = x1 - dy;
        final var y4 = y1 - dx;
        final var x5 = x4 + (dx - dy) / angleSlider1.getValue();
        final var y5 = y4 - (dx + dy) / angleSlider2.getValue();

        final Path2D square = new Path2D.Float();
        square.moveTo(x1, y1);
        square.lineTo(x2, y2);
        square.lineTo(x3, y3);
        square.lineTo(x4, y4);
        square.closePath();

        final var hue = 0.15F;
        final var hsbColor1 = Color.getHSBColor(hue + depth * 0.02F, 1, 1);
        graphics2D.setColor(hsbColor1);
        graphics2D.fill(square);
        graphics2D.setColor(hsbColor1.darker());
        graphics2D.draw(square);

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

        drawTree(graphics2D, x4, y4, x5, y5, depth + 1);
        drawTree(graphics2D, x5, y5, x3, y3, depth + 1);
    }

    @Override
    public void paintComponent(final Graphics graphics) {
        super.paintComponent(graphics);
        drawTree(graphics);
    }

    /**
     * Initial square of tree
     * <p>
     * X----------X
     * |          |
     * |          |
     * |          |
     * 1----------2
     */
    private void drawTree(final Graphics graphics) {
        final var X1 = (WINDOW_WIDTH / 2F) - (INIT_SQUARE_SIDE_LENGTH / 2F);
        final var Y1 = WINDOW_HEIGHT - 20;
        final var X2 = X1 + INIT_SQUARE_SIDE_LENGTH;
        LOGGER.info("Init square: X1: {}, Y1: {}, X2: {}, Y2: {}", X1, Y1, X2, Y1);
        drawTree((Graphics2D) graphics, X1, Y1, X2, Y1, 0);
    }

    public static void main(final String[] args) {
        final var pythagorasTree = new PythagorasTree();
        SwingUtilities.invokeLater(pythagorasTree::run);
    }

    private void run() {
        final var frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Pythagoras Tree");
        frame.setResizable(false);
        frame.add(this, BorderLayout.CENTER);
        final var sliderPanel = new JPanel();
        sliderPanel.add(angleSlider1, BorderLayout.WEST);
        sliderPanel.add(angleSlider2, BorderLayout.CENTER);
        sliderPanel.add(treeDepthSilder, BorderLayout.EAST);
        sliderPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        angleSlider1.addChangeListener(this::drawTreeAfterChange);
        angleSlider2.addChangeListener(this::drawTreeAfterChange);
        treeDepthSilder.addChangeListener(this::drawTreeAfterChange);

        configureSlider(angleSlider1, 1);
        configureSlider(angleSlider2, 1);
        configureSlider(treeDepthSilder, 2);

        frame.add(sliderPanel, BorderLayout.PAGE_END);
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
            paintComponent(getGraphics());
        }
    }
}
