package hzt;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.stream.DoubleStream;

import static java.lang.Math.*;

/**
 * <a href="https://www.youtube.com/watch?v=p7IGZTjC008">Coding Challenge 183: Paper Marbling Algorithm</a>
 */
public final class Marbling {

    private static final Dimension preferredSize = new Dimension(800, 1100);

    private final Canvas canvas = new Canvas();
    private final List<Drop> drops = new ArrayList<>();
    private final RandomGenerator random;

    public Marbling(final RandomGenerator random) {
        this.random = random;
    }

    public static void main(final String[] args) {
        final var marbling = new Marbling(new Random());
        SwingUtilities.invokeLater(marbling::start);
    }

    private void start() {
        final var frame = new JFrame("Marbling");
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
            public void mousePressed(final MouseEvent event) {
                updateCanvas(new Point2D(event.getX(), event.getY()), getDrawGraphics());
            }
        }));
        canvas.setIgnoreRepaint(true);
        drawBackground(getDrawGraphics());
    }

    private void updateCanvas(final Point2D mousePosition, final Graphics2D g) {
        drawBackground(g);
        final var newDrop = createDrop(mousePosition);
        drops.add(newDrop);

        for (final var drop : drops) {
            if (!drop.equals(newDrop)) {
                drop.marbledBy(newDrop);
            }
            g.setColor(drop.color);
            final var vertices = drop.vertices;
            final var xPoints = new int[vertices.length];
            final var yPoints = new int[vertices.length];
            for (var i = 0; i < vertices.length; i++) {
                xPoints[i] = (int) vertices[i].x;
                yPoints[i] = (int) vertices[i].y;
            }
            g.fillPolygon(xPoints, yPoints, xPoints.length);
            g.setColor(drop.color.darker());
            g.drawPolygon(xPoints, yPoints, yPoints.length);
        }
        cleanup(g);
    }

    private void cleanup(final Graphics2D g) {
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

    private @NotNull Drop createDrop(final Point2D mousePosition) {
        final var color = new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
        final var radius = random.nextInt(20, 100);
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

        Drop(final double radius, final Point2D center, final Color color) {
            this.radius = radius;
            this.center = center;
            this.color = color;
            vertices = createCircle(radius, center);
        }

        private Point2D[] createCircle(final double radius, final Point2D position) {
            return DoubleStream.iterate(0.0, d -> d + 1.0)
                    .limit(DETAIL)
                    .map(d -> 2 * PI * (d / DETAIL))
                    .mapToObj(angle -> position.add(cos(angle) * radius, sin(angle) * radius))
                    .toArray(Point2D[]::new);
        }

        public void marbledBy(final Drop other) {
            for (var i = 0; i < vertices.length; i++) {
                final var c = other.center;
                final var r = other.radius;
                final var diff = vertices[i].subtract(c);
                final var mSquared = diff.magnitudeSquared();
                final var root = Math.sqrt(1 + ((r * r) / mSquared));
                vertices[i] = c.add(diff.multiply(root));
            }
        }
    }

    record Point2D(double x, double y) {

        public Point2D add(final Point2D other) {
            return add(other.x, other.y);
        }

        private Point2D add(final double x, final double y) {
            return new Point2D(this.x + x, this.y + y);
        }

        public Point2D subtract(final Point2D other) {
            return new Point2D(x - other.x, y - other.y);
        }

        public Point2D multiply(final double factor) {
            return new Point2D(x * factor, y * factor);
        }

        public double magnitudeSquared() {
            return x * x + y * y;
        }
    }
}
