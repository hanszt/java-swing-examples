package hzt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * See <a href="http://blog.rogach.org/2015/08/how-to-create-your-own-simple-3d-render.html">How to create your own simple 3D render engine in pure Java</a>
 */
public final class Demo3DRendering {

    private static final Logger logger = LoggerFactory.getLogger(Demo3DRendering.class);

    void main() {
        final var frame = new JFrame();
        final var pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());
        final var bottomControlPanel = new JPanel(new GridLayout(3, 1));

        // slider to control horizontal rotation
        final var headingSlider = new JSlider(-180, 180, 0);
        bottomControlPanel.add(headingSlider, BorderLayout.NORTH);

        // slider to control inflation
        final var inflationSlider = new JSlider(0, 6, 4);
        bottomControlPanel.add(inflationSlider, BorderLayout.SOUTH);
        enum Mode {WIRE_FRAME, FILLED_SHADED, FILLED_UNSHADED}
        final var modeButton = new Button() {
            Mode mode = Mode.FILLED_SHADED;

            {
                setLabel(getLabel(mode));
                addActionListener(_ -> {
                    final var newMode = switch (mode) {
                        case WIRE_FRAME -> Mode.FILLED_UNSHADED;
                        case FILLED_SHADED -> Mode.WIRE_FRAME;
                        case FILLED_UNSHADED -> Mode.FILLED_SHADED;
                    };
                    logger.info("Switching to mode {}", newMode);
                    mode = newMode;
                    setLabel(getLabel(newMode));
                });
            }

            private static String getLabel(final Mode mode) {
                final var name = mode.name();
                return name.charAt(0) + name.substring(1).replace("_", " ").toLowerCase();
            }
        };
        bottomControlPanel.add(modeButton, BorderLayout.CENTER);

        pane.add(bottomControlPanel, BorderLayout.SOUTH);

        // slider to control vertical rotation
        final var pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        // panel to display render results
        final var renderPanel = new JPanel() {
            @Override
            public void paintComponent(final Graphics g) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
                var mode = switch (modeButton.mode) {
                    case Mode.WIRE_FRAME -> drawWireframe(g);
                    case Mode.FILLED_SHADED -> drawFilledShaded(g);
                    case Mode.FILLED_UNSHADED -> drawFilledUnShaded(g);
                };
                logger.trace("Mode: {}", mode);
            }

            private Mode drawWireframe(final Graphics graphics) {
                final var rotationMatrix = buildRotationMatrix();
                final var tris = inflate(buildTetrahedron());

                final var g2d = (Graphics2D) graphics;
                g2d.translate(getWidth() / 2, getHeight() / 2);
                g2d.setColor(Color.WHITE);
                for (Triangle t : tris) {
                    Vertex v1 = rotationMatrix.apply(t.v1());
                    Vertex v2 = rotationMatrix.apply(t.v2());
                    Vertex v3 = rotationMatrix.apply(t.v3());
                    Path2D path = new Path2D.Double();
                    path.moveTo(v1.x(), v1.y());
                    path.lineTo(v2.x(), v2.y());
                    path.lineTo(v3.x(), v3.y());
                    path.closePath();
                    g2d.draw(path);
                }
                return Mode.WIRE_FRAME;
            }

            private Mode drawFilledShaded(final Graphics graphics) {
                drawFilled(graphics, Demo3DRendering::shade);
                return Mode.FILLED_SHADED;
            }

            private Mode drawFilledUnShaded(final Graphics graphics) {
                drawFilled(graphics, (color, _) -> color);
                return Mode.FILLED_UNSHADED;
            }

            private void drawFilled(final Graphics g, Shader shader) {
                final var rotationMatrix = buildRotationMatrix();
                final var tris = inflate(buildTetrahedron());
                final var img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

                // initialize array with extremely far away depths
                final var zBuffer = new double[img.getWidth() * img.getHeight()];
                Arrays.fill(zBuffer, Double.NEGATIVE_INFINITY);

                for (final var t : tris) {
                    final var v1 = rotationMatrix.apply(t.v1()).plus(getWidth() / 2.0, getHeight() / 2.0, 0);
                    final var v2 = rotationMatrix.apply(t.v2()).plus(getWidth() / 2.0, getHeight() / 2.0, 0);
                    final var v3 = rotationMatrix.apply(t.v3()).plus(getWidth() / 2.0, getHeight() / 2.0, 0);

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
                                    img.setRGB(x, y, shader.shade(t.color(), angleCos).getRGB());
                                    zBuffer[zIndex] = depth;
                                }
                            }
                        }
                    }
                }
                g.drawImage(img, 0, 0, null);
            }

            private Matrix3 buildRotationMatrix() {
                final var heading = Math.toRadians(headingSlider.getValue());
                final var headingTransform = headingTransform(heading);
                final var pitch = Math.toRadians(pitchSlider.getValue());
                final var pitchTransform = pitchTransform(pitch);
                return headingTransform.multiply(pitchTransform);
            }

            private List<Triangle> inflate(final List<Triangle> initTris) {
                var tris = initTris;
                for (var i = 0; i < inflationSlider.getValue(); i++) {
                    tris = tris.stream()
                            .flatMap(Triangle::inflate)
                            .map(t -> t.resizeBy(sqrt30000))
                            .toList();
                }
                return tris;
            }

            private static Matrix3 pitchTransform(final double pitch) {
                return new Matrix3(
                        1, 0, 0,
                        0, Math.cos(pitch), Math.sin(pitch),
                        0, -Math.sin(pitch), Math.cos(pitch)
                );
            }

            private static Matrix3 headingTransform(final double heading) {
                return new Matrix3(
                        Math.cos(heading), 0, -Math.sin(heading),
                        0, 1, 0,
                        Math.sin(heading), 0, Math.cos(heading)
                );
            }

            private static List<Triangle> buildTetrahedron() {
                return List.of(
                        new Triangle(
                                new Vertex(100, 100, 100),
                                new Vertex(-100, -100, 100),
                                new Vertex(-100, 100, -100),
                                Color.WHITE),
                        new Triangle(
                                new Vertex(100, 100, 100),
                                new Vertex(-100, -100, 100),
                                new Vertex(100, -100, -100),
                                Color.RED),
                        new Triangle(
                                new Vertex(-100, 100, -100),
                                new Vertex(100, -100, -100),
                                new Vertex(100, 100, 100),
                                Color.GREEN),
                        new Triangle(
                                new Vertex(-100, 100, -100),
                                new Vertex(100, -100, -100),
                                new Vertex(-100, -100, 100),
                                Color.BLUE));
            }
        };
        pane.add(renderPanel, BorderLayout.CENTER);

        headingSlider.addChangeListener(_ -> renderPanel.repaint());
        pitchSlider.addChangeListener(_ -> renderPanel.repaint());
        inflationSlider.addChangeListener(_ -> renderPanel.repaint());
        modeButton.addActionListener(_ -> renderPanel.repaint());

        frame.setSize(600, 600);
        frame.setVisible(true);
    }

    static Color shade(final Color color, final double shade) {
        final var exponent = 2.4;
        final var redLinear = Math.pow(color.getRed(), exponent) * shade;
        final var greenLinear = Math.pow(color.getGreen(), exponent) * shade;
        final var blueLinear = Math.pow(color.getBlue(), exponent) * shade;

        final var red = (int) Math.pow(redLinear, 1 / exponent);
        final var green = (int) Math.pow(greenLinear, 1 / exponent);
        final var blue = (int) Math.pow(blueLinear, 1 / exponent);

        return new Color(red, green, blue);
    }

    final double sqrt30000 = Math.sqrt(30_000);
}

record Vertex(double x, double y, double z) {

    static final Vertex ZERO = new Vertex(0, 0, 0);

    Vertex plus(double x, double y, double z) {
        return new Vertex(this.x + x, this.y + y, this.z + z);
    }

    Vertex divided(double n) {
        return Double.compare(n, 0.0) == 0 ? ZERO : new Vertex(x / n, y / n, z / n);
    }

    Vertex multiplied(final double factor) {
        return new Vertex(x * factor, y * factor, z * factor);
    }

    Vertex crossProduct(Vertex other) {
        return new Vertex(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }

    Vertex normalized() {
        final var mag = magnitude();
        return Double.compare(mag, 0.0) == 0 ? ZERO : divided(mag);
    }

    double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }
}

record Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {

    Triangle resizeBy(double factor) {
        final var v1n = v1.divided(v1.magnitude()).multiplied(factor);
        final var v2n = v2.divided(v2.magnitude()).multiplied(factor);
        final var v3n = v3.divided(v3.magnitude()).multiplied(factor);
        return new Triangle(v1n, v2n, v3n, color);
    }

    Stream<Triangle> inflate() {
        final var m1 = new Vertex((v1.x() + v2.x()) / 2, (v1.y() + v2.y()) / 2, (v1.z() + v2.z()) / 2);
        final var m2 = new Vertex((v2.x() + v3.x()) / 2, (v2.y() + v3.y()) / 2, (v2.z() + v3.z()) / 2);
        final var m3 = new Vertex((v1.x() + v3.x()) / 2, (v1.y() + v3.y()) / 2, (v1.z() + v3.z()) / 2);
        return Stream.of(
                new Triangle(v1, m1, m3, color),
                new Triangle(v2, m1, m2, color),
                new Triangle(v3, m2, m3, color),
                new Triangle(m1, m2, m3, color)
        );
    }
}

/**
 * Using a 3 by 3 matrix allows rotation. Adding translation requires a 4 by 4 matrix. Related: Quaternions.
 *
 * @param values the values of the 3 by 3 matrix.
 */
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

    Vertex apply(final Vertex v) {
        return new Vertex(
                v.x() * values[0] + v.y() * values[3] + v.z() * values[6],
                v.x() * values[1] + v.y() * values[4] + v.z() * values[7],
                v.x() * values[2] + v.y() * values[5] + v.z() * values[8]
        );
    }
}

@FunctionalInterface
interface Shader {
    Color shade(Color color, double factor);
}
