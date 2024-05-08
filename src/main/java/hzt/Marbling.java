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
public final class Marbling {

    private static final Random RANDOM = new Random();
    private static final Dimension preferredSize = new Dimension(800, 1100);

    private final Canvas canvas = new Canvas();
    private final List<Drop> drops = new ArrayList<>();


    public static void main(String[] args) {
        final var marbling = new Marbling();
        SwingUtilities.invokeLater(marbling::start);
    }

    private void start() {
        final JFrame frame = new JFrame("Marbling");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        final var panel = new JPanel();
        panel.setPreferredSize(preferredSize);
        canvas.setPreferredSize(preferredSize);
        panel.add(canvas);
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        canvas.createBufferStrategy(2);
        canvas.addMouseListener((new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                updateCanvas(new Point2D(event.getX(), event.getY()), getDrawGraphics());
            }
        }));
        canvas.setIgnoreRepaint(true);
        drawBackground(getDrawGraphics());
    }

    private void updateCanvas(Point2D mousePosition, Graphics2D g) {
        drawBackground(g);
        final var newDrop = createDrop(mousePosition);
        for (final var drop : drops) {
            drop.marbledBy(newDrop);
        }
        drops.add(newDrop);

        for (final Drop drop : drops) {
            g.setColor(drop.color);
            final var xPoints = drop.xPoints();
            final var yPoints = drop.yPoints();
            g.fillPolygon(xPoints, yPoints, drop.vertices.length);
            g.setColor(drop.color.darker());
            g.drawPolygon(xPoints, yPoints, drop.vertices.length);
        }
        cleanup(g);
    }

    private void cleanup(Graphics2D g) {
        g.dispose();
        // blit/flip the buffer
        final var strategy = this.canvas.getBufferStrategy();
        if (!strategy.contentsLost()) {
            strategy.show();
        }
        // Sync the display on some systems.
        // (on Linux, this fixes event queue problems)
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawBackground(final Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, preferredSize.width, preferredSize.height);
    }

    private @NotNull Drop createDrop(Point2D mousePosition) {
        final var color = new Color(RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255));
        final var radius = RANDOM.nextInt(20, 100);
        return new Drop(radius, mousePosition, color);
    }

    private Graphics2D getDrawGraphics() {
        return (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
    }

    static final class Drop {
        private static final int DETAIL = 500;
        private final double radius;

        private final Color color;
        private final Point2D center;
        private final Point2D[] vertices;

        Drop(double radius, Point2D center, Color color) {
            this.radius = radius;
            this.center = center;
            this.color = color;
            vertices = createCircle(radius, center);
        }

        private Point2D[] createCircle(double radius, Point2D position) {
            return DoubleStream.iterate(0, d -> d + 1.0)
                    .limit(DETAIL)
                    .map(d -> 2 * PI * (d / DETAIL))
                    .mapToObj(angle -> new Point2D(cos(angle) * radius + position.x(), sin(angle) * radius + position.y()))
                    .toArray(Point2D[]::new);
        }

        public void marbledBy(Drop other) {
            for (int i = 0; i < vertices.length; i++) {
                var c = other.center;
                var r = other.radius;
                final var diff = vertices[i].subtract(c);
                var mSquared = diff.magnitudeSquared();
                var root = Math.sqrt(1 + ((r * r) / mSquared));
                vertices[i] = c.add(diff.multiply(root));
            }
        }

        public int[] xPoints() {
            return Arrays.stream(vertices).mapToInt(p -> (int) p.x).toArray();
        }

        public int[] yPoints() {
            return Arrays.stream(vertices).mapToInt(p -> (int) p.y).toArray();
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
