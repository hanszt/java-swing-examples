package hzt;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * See <a href="http://blog.rogach.org/2015/08/how-to-create-your-own-simple-3d-render.html">How to create your own simple 3D render engine in pure Java</a>
 */
public final class Demo3DRendering {

    void main() {
        final var frame = new JFrame();
        final var pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // slider to control horizontal rotation
        final var headingSlider = new JSlider(-180, 180, 0);
        pane.add(headingSlider, BorderLayout.SOUTH);

        // slider to control vertical rotation
        final var pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        // panel to display render results
        final var renderPanel = new JPanel() {
            public void paintComponent(final Graphics g) {
                final var g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                List<Triangle> tris = new ArrayList<>();
                tris.add(new Triangle(
                        new Vertex(100, 100, 100),
                        new Vertex(-100, -100, 100),
                        new Vertex(-100, 100, -100),
                        Color.WHITE));
                tris.add(new Triangle(
                        new Vertex(100, 100, 100),
                        new Vertex(-100, -100, 100),
                        new Vertex(100, -100, -100),
                        Color.RED));
                tris.add(new Triangle(
                        new Vertex(-100, 100, -100),
                        new Vertex(100, -100, -100),
                        new Vertex(100, 100, 100),
                        Color.GREEN));
                tris.add(new Triangle(
                        new Vertex(-100, 100, -100),
                        new Vertex(100, -100, -100),
                        new Vertex(-100, -100, 100),
                        Color.BLUE));

                for (var i = 0; i < 4; i++) {
                    tris = inflate(tris);
                }

                final var heading = Math.toRadians(headingSlider.getValue());
                final var headingTransform = new Matrix3(
                        Math.cos(heading), 0, -Math.sin(heading),
                        0, 1, 0,
                        Math.sin(heading), 0, Math.cos(heading)
                );
                final var pitch = Math.toRadians(pitchSlider.getValue());
                final var pitchTransform = new Matrix3(
                        1, 0, 0,
                        0, Math.cos(pitch), Math.sin(pitch),
                        0, -Math.sin(pitch), Math.cos(pitch)
                );
                final var transform = headingTransform.multiply(pitchTransform);

                final var img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

                final var zBuffer = new double[img.getWidth() * img.getHeight()];
                // initialize array with extremely far away depths
                Arrays.fill(zBuffer, Double.NEGATIVE_INFINITY);

                for (final var t : tris) {
                    final var v1 = transform.transform(t.v1())
                            .plus(new Vertex(getWidth() / 2.0, getHeight() / 2.0, 0));
                    final var v2 = transform.transform(t.v2())
                            .plus(new Vertex(getWidth() / 2.0, getHeight() / 2.0, 0));
                    final var v3 = transform.transform(t.v3())
                            .plus(new Vertex(getWidth() / 2.0, getHeight() / 2.0, 0));

                    final var ab = new Vertex(v2.x() - v1.x(), v2.y() - v1.y(), v2.z() - v1.z());
                    final var ac = new Vertex(v3.x() - v1.x(), v3.y() - v1.y(), v3.z() - v1.z());
                    final var norm = ab.crossProduct(ac).normalized();

                    final var angleCos = Math.abs(norm.z());

                    final var minX = (int) Math.max(0, Math.ceil(Math.min(v1.x(), Math.min(v2.x(), v3.x()))));
                    final var maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x(), Math.max(v2.x(), v3.x()))));
                    final var minY = (int) Math.max(0, Math.ceil(Math.min(v1.y(), Math.min(v2.y(), v3.y()))));
                    final var maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y(), Math.max(v2.y(), v3.y()))));

                    final var triangleArea = (v1.y() - v3.y()) * (v2.x() - v3.x()) + (v2.y() - v3.y()) * (v3.x() - v1.x());

                    for (var y = minY; y <= maxY; y++) {
                        for (var x = minX; x <= maxX; x++) {
                            final var b1 = ((y - v3.y()) * (v2.x() - v3.x()) + (v2.y() - v3.y()) * (v3.x() - x)) / triangleArea;
                            final var b2 = ((y - v1.y()) * (v3.x() - v1.x()) + (v3.y() - v1.y()) * (v1.x() - x)) / triangleArea;
                            final var b3 = ((y - v2.y()) * (v1.x() - v2.x()) + (v1.y() - v2.y()) * (v2.x() - x)) / triangleArea;
                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                                final var depth = b1 * v1.z() + b2 * v2.z() + b3 * v3.z();
                                final var zIndex = y * img.getWidth() + x;
                                if (zBuffer[zIndex] < depth) {
                                    img.setRGB(x, y, getShade(t.color(), angleCos).getRGB());
                                    zBuffer[zIndex] = depth;
                                }
                            }
                        }
                    }
                }
                g2.drawImage(img, 0, 0, null);
            }
        };
        pane.add(renderPanel, BorderLayout.CENTER);

        headingSlider.addChangeListener(_ -> renderPanel.repaint());
        pitchSlider.addChangeListener(_ -> renderPanel.repaint());

        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public static Color getShade(final Color color, final double shade) {
        final var redLinear = Math.pow(color.getRed(), 2.4) * shade;
        final var greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        final var blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        final var red = (int) Math.pow(redLinear, 1 / 2.4);
        final var green = (int) Math.pow(greenLinear, 1 / 2.4);
        final var blue = (int) Math.pow(blueLinear, 1 / 2.4);

        return new Color(red, green, blue);
    }

    List<Triangle> inflate(List<Triangle> tris) {
        List<Triangle> result = new ArrayList<>();
        for (final var t : tris) {
            final var m1 = new Vertex((t.v1().x() + t.v2().x()) / 2, (t.v1().y() + t.v2().y()) / 2, (t.v1().z() + t.v2().z()) / 2);
            final var m2 = new Vertex((t.v2().x() + t.v3().x()) / 2, (t.v2().y() + t.v3().y()) / 2, (t.v2().z() + t.v3().z()) / 2);
            final var m3 = new Vertex((t.v1().x() + t.v3().x()) / 2, (t.v1().y() + t.v3().y()) / 2, (t.v1().z() + t.v3().z()) / 2);
            result.add(new Triangle(t.v1(), m1, m3, t.color()));
            result.add(new Triangle(t.v2(), m1, m2, t.color()));
            result.add(new Triangle(t.v3(), m2, m3, t.color()));
            result.add(new Triangle(m1, m2, m3, t.color()));
        }
        return getTriangles(result);
    }

    private static @NotNull List<Triangle> getTriangles(final List<Triangle> result) {
        final var sqrt30000 = Math.sqrt(30_000);
        final List<Triangle> triangles = new ArrayList<>();
        for (Triangle t : result) {
            final var v1 = t.v1();
            final var v1n = v1.divided(v1.length() / sqrt30000);
            final var v2 = t.v2();
            final var v2n = v2.divided(v2.length() / sqrt30000);
            final var v3 = t.v3();
            final var v3n = v3.divided(v3.length() / sqrt30000);
            triangles.add(new Triangle(v1n, v2n, v3n, t.color()));
        }
        return triangles;
    }
}

record Vertex(double x, double y, double z) {
    Vertex plus(Vertex other) {
        return new Vertex(x + other.x, y + other.y, z + other.z);
    }

    Vertex divided(double n) {
        return new Vertex(x / n, y / n, z / n);
    }

    Vertex crossProduct(Vertex other) {
        return new Vertex(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }

    Vertex normalized() {
        return divided(length());
    }

    double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

}

record Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
}

record Matrix3(double... values) {

    Matrix3 {
        if (values.length != 9) {
            throw new IllegalArgumentException("Must have 9 values!");
        }
        values = Arrays.copyOf(values, values.length);
    }

    Matrix3 multiply(final Matrix3 other) {
        final var result = new double[9];
        for (var row = 0; row < 3; row++) {
            for (var col = 0; col < 3; col++) {
                for (var i = 0; i < 3; i++) {
                    result[row * 3 + col] += this.values[row * 3 + i] * other.values[i * 3 + col];
                }
            }
        }
        return new Matrix3(result);
    }

    Vertex transform(final Vertex in) {
        return new Vertex(
                in.x() * values[0] + in.y() * values[3] + in.z() * values[6],
                in.x() * values[1] + in.y() * values[4] + in.z() * values[7],
                in.x() * values[2] + in.y() * values[5] + in.z() * values[8]
        );
    }
}
