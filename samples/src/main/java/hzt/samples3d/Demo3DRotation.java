package hzt.samples3d;

import org.hzt.utils.sequences.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.function.Consumer;

import static java.lang.Math.*;

/**
 * See <a href="http://blog.rogach.org/2015/08/how-to-create-your-own-simple-3d-render.html">How to create your own simple 3D render engine in pure Java</a>
 */
public final class Demo3DRotation {

    private static final Logger logger = LoggerFactory.getLogger(Demo3DRotation.class);

    public static void main(String[] args) {
        enum Mode {WIRE_FRAME, FILLED_SHADED, FILLED_UNSHADED}
        final var frame = new JFrame();
        final var pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        final var modeButton = new Button() {
            Mode mode = Mode.FILLED_SHADED;

            {
                setLabel(getLabel(mode));
                addActionListener(unused -> {
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

        pane.add(bottomControlPanel, BorderLayout.SOUTH);
        pane.add(pitchSlider, BorderLayout.EAST);

        // panel to display render results
        final var renderPanel = new JPanel() {

            @Override
            public void paintComponent(final Graphics g) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                final var mode = switch (modeButton.mode) {
                    case Mode.WIRE_FRAME -> drawWireframe(g);
                    case Mode.FILLED_SHADED -> drawFilledShaded(g);
                    case Mode.FILLED_UNSHADED -> drawFilledUnShaded(g);
                };
                logger.trace("Painting with mode: {}", mode);
            }

            private Mode drawWireframe(final Graphics graphics) {
                final var rotationMatrix = buildRotationMatrix();

                final var g2d = (Graphics2D) graphics;
                g2d.translate(getWidth() / 2, getHeight() / 2);
                g2d.setColor(Color.WHITE);
                useTriangles(t -> {
                    final var v1 = rotationMatrix.apply(t.v1());
                    final var v2 = rotationMatrix.apply(t.v2());
                    final var v3 = rotationMatrix.apply(t.v3());
                    final Path2D path = new Path2D.Double();
                    path.moveTo(v1.x(), v1.y());
                    path.lineTo(v2.x(), v2.y());
                    path.lineTo(v3.x(), v3.y());
                    path.closePath();
                    g2d.draw(path);
                });
                return Mode.WIRE_FRAME;
            }

            private Mode drawFilledShaded(final Graphics graphics) {
                final var image = build3DShapeImage(this::shade);
                graphics.drawImage(image, 0, 0, null);
                return Mode.FILLED_SHADED;
            }

            private Color shade(final Color color, final double shade) {
                final var exponent = shadingSlider.getValue() / 100.0;
                final var redLinear = Math.pow(color.getRed(), exponent) * shade;
                final var greenLinear = Math.pow(color.getGreen(), exponent) * shade;
                final var blueLinear = Math.pow(color.getBlue(), exponent) * shade;

                final var red = (int) Math.pow(redLinear, 1 / exponent);
                final var green = (int) Math.pow(greenLinear, 1 / exponent);
                final var blue = (int) Math.pow(blueLinear, 1 / exponent);

                return new Color(red, green, blue);
            }

            private Mode drawFilledUnShaded(final Graphics graphics) {
                final var image = build3DShapeImage((color, unused) -> color);
                graphics.drawImage(image, 0, 0, null);
                return Mode.FILLED_UNSHADED;
            }

            private Image build3DShapeImage(final Shader shader) {
                final var width = getWidth();
                final var height = getHeight();
                final var img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                final var rotationMatrix = buildRotationMatrix();

                // initialize array with extremely far away depths
                final var zBuffer = new double[width * height];
                Arrays.fill(zBuffer, Double.NEGATIVE_INFINITY);

                useTriangles(t -> {
                    final var v1 = rotationMatrix.apply(t.v1()).plus(width / 2.0, height / 2.0, 0);
                    final var v2 = rotationMatrix.apply(t.v2()).plus(width / 2.0, height / 2.0, 0);
                    final var v3 = rotationMatrix.apply(t.v3()).plus(width / 2.0, height / 2.0, 0);

                    final var norm = v2.minus(v1).crossProduct(v3.minus(v1)).normalized();
                    final var angleCos = Math.abs(norm.z());

                    final var minX = (int) max(0, ceil(min(v1.x(), min(v2.x(), v3.x()))));
                    final var maxX = (int) min(width - 1, floor(max(v1.x(), max(v2.x(), v3.x()))));
                    final var minY = (int) max(0, ceil(min(v1.y(), min(v2.y(), v3.y()))));
                    final var maxY = (int) min(height - 1, floor(max(v1.y(), max(v2.y(), v3.y()))));

                    final var triangleArea = (v1.y() - v3.y()) * (v2.x() - v3.x()) + (v2.y() - v3.y()) * (v3.x() - v1.x());

                    for (var y = minY; y <= maxY; y++) {
                        for (var x = minX; x <= maxX; x++) {
                            final var b1 = ((y - v3.y()) * (v2.x() - v3.x()) + (v2.y() - v3.y()) * (v3.x() - x)) / triangleArea;
                            final var b2 = ((y - v1.y()) * (v3.x() - v1.x()) + (v3.y() - v1.y()) * (v1.x() - x)) / triangleArea;
                            final var b3 = ((y - v2.y()) * (v1.x() - v2.x()) + (v1.y() - v2.y()) * (v2.x() - x)) / triangleArea;
                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                                final var depth = b1 * v1.z() + b2 * v2.z() + b3 * v3.z();
                                final var zIndex = y * width + x;
                                if (zBuffer[zIndex] < depth) {
                                    img.setRGB(x, y, shader.shade(t.color(), angleCos).getRGB());
                                    zBuffer[zIndex] = depth;
                                }
                            }
                        }
                    }
                });
                return img;
            }

            private Matrix3 buildRotationMatrix() {
                final var heading = Math.toRadians(headingSlider.getValue());
                final var pitch = Math.toRadians(pitchSlider.getValue());
                return headingTransform(heading).multiply(pitchTransform(pitch));
            }

            private Sequence<Triangle> inflate() {
                return Sequence.iterate(tetrahedronTriangles, s -> s.flatMap(Triangle::midPointTriangles))
                        .take(inflationSlider.getValue())
                        .last()
                        .map(t -> t.normalizeAndResizeBy(zoomSlider.getValue()));
            }

            private void useTriangles(Consumer<Triangle> consumer) {
                final var count = inflate().onEach(consumer).count();
                if (inflationSlider.getValueIsAdjusting()) {
                    logger.info("Nr of triangles: {}", count);
                }
            }
        };
        pane.add(renderPanel, BorderLayout.CENTER);

        headingSlider.addChangeListener(unused -> renderPanel.repaint());
        pitchSlider.addChangeListener(unused -> renderPanel.repaint());
        zoomSlider.addChangeListener(unused -> renderPanel.repaint());
        inflationSlider.addChangeListener(unused -> renderPanel.repaint());
        shadingSlider.addChangeListener(unused -> renderPanel.repaint());
        modeButton.addActionListener(unused -> renderPanel.repaint());

        frame.setSize(600, 600);
        frame.setVisible(true);
    }

    private static Matrix3 pitchTransform(final double pitch) {
        return new Matrix3(
                1.0, 0.0, 0.0,
                0.0, Math.cos(pitch), Math.sin(pitch),
                0.0, -Math.sin(pitch), Math.cos(pitch)
        );
    }

    private static Matrix3 headingTransform(final double heading) {
        return new Matrix3(
                Math.cos(heading), 0.0, -Math.sin(heading),
                0.0, 1.0, 0.0,
                Math.sin(heading), 0.0, Math.cos(heading)
        );
    }

    /**
     * A tetrahedron generating sequence with the center around 0,0,0
     */
    private static final Sequence<Triangle> tetrahedronTriangles = Sequence.of(
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
                    new Vertex(-1, 1, -1),
                    new Vertex(1, -1, -1),
                    new Vertex(-1, -1, 1),
                    Color.BLUE));
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

