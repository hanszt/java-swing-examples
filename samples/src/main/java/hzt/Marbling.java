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

    private static final Dimension preferredCanvasSize = new Dimension(800, 1100);

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
        canvas.setPreferredSize(preferredCanvasSize);
        panel.add(canvas);
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        canvas.createBufferStrategy(2);
        canvas.addMouseListener((new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent event) {
                placeNewDrop(new Point2D(event.getX(), event.getY()), getDrawGraphics());
            }
        }));
        canvas.setIgnoreRepaint(true);
        drawBackground(getDrawGraphics());
    }

    private void placeNewDrop(final Point2D mousePosition, final Graphics2D g) {
        drawBackground(g);
        final var newDrop = createDrop(mousePosition);
        drops.add(newDrop);

        for (int j = 0; j < drops.size(); j++) {
            final var drop = drops.get(j);
            if (!drop.equals(newDrop)) {
                drops.set(j, drop.marbledBy(newDrop));
            }
            final var updatedDrop = drops.get(j);
            g.setColor(updatedDrop.color);
            final var vertices = updatedDrop.vertices;
            final var xPoints = new int[vertices.size()];
            final var yPoints = new int[vertices.size()];
            for (var i = 0; i < vertices.size(); i++) {
                xPoints[i] = (int) vertices.get(i).x;
                yPoints[i] = (int) vertices.get(i).y;
            }
            g.fillPolygon(xPoints, yPoints, xPoints.length);
            g.setColor(updatedDrop.color.darker());
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
        graphics.fillRect(0, 0, preferredCanvasSize.width, preferredCanvasSize.height);
    }

    private @NotNull Drop createDrop(final Point2D mousePosition) {
        final var color = new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
        final var radius = random.nextInt(20, 100);
        return new Drop(radius, mousePosition, color, Drop.DETAIL);
    }

    private Graphics2D getDrawGraphics() {
        return (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
    }

    static final class Drop {
        private static final int DETAIL = 500;

        private final double radius;
        private final Color color;
        private final Point2D position;
        final List<Point2D> vertices;

        Drop(final double radius, final Point2D position, final Color color, final int size) {
            this(radius, position, color, createCircle(radius, position, size));
        }

        private Drop(final double radius, final Point2D position, final Color color, List<Point2D> vertices) {
            this.radius = radius;
            this.position = position;
            this.color = color;
            this.vertices = vertices;
        }

        private static List<Point2D> createCircle(
                final double radius,
                final Point2D position,
                final int size
        ) {
            return DoubleStream.iterate(0.0, d -> d + 1.0)
                    .limit(size)
                    .map(d -> 2 * PI * (d / size))
                    .mapToObj(angle -> position.add(cos(angle) * radius, sin(angle) * radius))
                    .toList();
        }

        public Drop marbledBy(final Drop other) {
            final var vertices = this.vertices.stream()
                    .map(vertex -> updatedBy(other, vertex))
                    .toList();
            return new Drop(radius, position, color, vertices);
        }

        private @NotNull Point2D updatedBy(final Drop other, final Point2D vertex) {
            final var c = other.position;
            final var r = other.radius;
            final var diff = vertex.subtract(c);
            final var mSquared = diff.magnitudeSquared();
            final var root = sqrt(1 + ((r * r) / mSquared));
            return c.add(diff.multiply(root));
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
