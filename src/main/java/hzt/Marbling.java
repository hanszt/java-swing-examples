package hzt;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * <a href="https://www.youtube.com/watch?v=p7IGZTjC008">Coding Challenge 183: Paper Marbling Algorithm</a>
 */
public final class Marbling extends JPanel {

    private static final Random RANDOM = new Random();

    private final CanvasMouseListener mouseListener = new CanvasMouseListener();
    private final List<Drop> drops = new ArrayList<>();


    public static void main(String[] args) {
        final var marbling = new Marbling();
        SwingUtilities.invokeLater(marbling::start);
    }

    private void start() {
        final JFrame frame = new JFrame("Marbling");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        setPreferredSize(new Dimension(800, 1100));
        addMouseListener(mouseListener);
        frame.add(this, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (mouseListener.isPressed) {
            final var newDrop = createDrop();
            for (final var drop : drops) {
                drop.marbledBy(newDrop);
            }
            drops.add(newDrop);

            super.paintComponent(g);
            for (final Drop drop : drops) {
                g.setColor(drop.color);
                g.fillPolygon(drop.xPoints(), drop.yPoints(), drop.points.length);
            }
        }
    }

    private @NotNull Drop createDrop() {
        final var color = new Color(RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255));
        final var radius = RANDOM.nextInt(20, 100);
        final var center = new Point2D(mouseListener.x, mouseListener.y);
        return new Drop(radius, center, color);
    }

    private final class CanvasMouseListener extends MouseAdapter {

        boolean isPressed = false;
        int x = 0;
        int y = 0;

        @Override
        public void mousePressed(MouseEvent event) {
            isPressed = true;
            x = event.getX();
            y = event.getY();
            paintComponent(getGraphics());
        }
    }

    static final class Drop {
        private static final int RESOLUTION = 500;
        private final double radius;

        private final Color color;
        private final Point2D center;
        private final Point2D[] points;

        Drop(double radius, Point2D center, java.awt.Color color) {
            this.radius = radius;
            this.center = center;
            this.color = color;
            points = createCircle(radius, center);
        }

        private Point2D[] createCircle(double radius, Point2D position) {
            return DoubleStream.iterate(0, d -> d + 1.0)
                    .limit(RESOLUTION)
                    .map(d -> 2 * PI * (d / RESOLUTION))
                    .mapToObj(angle -> new Point2D(cos(angle) * radius + position.x(), sin(angle) * radius + position.y()))
                    .toArray(Point2D[]::new);
        }

        public void marbledBy(Drop other) {
            for (int i = 0; i < points.length; i++) {
                points[i] = marble(other, points[i]);
            }
        }

        private static Point2D marble(Drop other, Point2D p) {
            var c = other.center;
            var r = other.radius;
            final var diff = p.subtract(c);
            var mSquared = diff.magnitudeSquared();
            var root = Math.sqrt(1 + ((r * r) / mSquared));
            return c.add(diff.multiply(root));
        }

        public int[] xPoints() {
            return Arrays.stream(points).mapToInt(p -> (int) p.x).toArray();
        }

        public int[] yPoints() {
            return Arrays.stream(points).mapToInt(p -> (int) p.y).toArray();
        }
    }

    record Point2D(double x, double y) {


        public Point2D add(Point2D other) {
            return new Point2D(x + other.x, y + other.y);
        }

        public Point2D subtract(Point2D other) {
            return new Point2D(x - other.x, y - other.y);
        }

        public Point2D multiply(double factor) {
            return new Point2D(x * factor, y * factor);
        }

        public double magnitudeSquared() {
            return x * x + y * y;
        }
    }
}
