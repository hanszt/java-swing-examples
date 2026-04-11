package hzt.samples3d;

import hzt.samples3d.RenderingModeButton.Mode;
import org.hzt.utils.sequences.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.Math.*;

/**
 * See <a href="https://www.youtube.com/watch?v=d4EgbgTm0Bg">Visualizing quaternions (4d numbers) with stereographic projection</a>
 * See <a href="http://blog.rogach.org/2015/08/how-to-create-your-own-simple-3d-render.html">How to create your own simple 3D render engine in pure Java</a>
 */
public final class Demo3DQuaternions {

    private static final Logger logger = LoggerFactory.getLogger(Demo3DQuaternions.class);

    private double translateX = 0;
    private double translateY = 0;
    private double translateZ = 0;

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

        // panel to display render results
        final var renderPanel = new JPanel() {

            private transient List<Triangle> shape = createShape();

            /**
             * Called when repaint is called
             *
             * @param g the <code>Graphics</code> object to protect
             */
            @Override
            public void paintComponent(final Graphics g) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                final var mode = switch (modeButton.getMode()) {
                    case Mode.WIRE_FRAME -> drawWireframe(g);
                    case Mode.FILLED_SHADED -> drawFilledShaded(g);
                    case Mode.FILLED_UNSHADED -> drawFilledUnShaded(g);
                };
                logger.trace("Painting with mode: {}", mode);
            }

            private Mode drawWireframe(final Graphics graphics) {
                final var transform = buildTransform();

                final var g2d = (Graphics2D) graphics;
                g2d.translate(getWidth() / 2, getHeight() / 2);
                g2d.setColor(Color.WHITE);
                useTriangles(t -> {
                    var v1 = transform.apply(t.v1());
                    var v2 = transform.apply(t.v2());
                    var v3 = transform.apply(t.v3());

                    v1 = project(v1);
                    v2 = project(v2);
                    v3 = project(v3);

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
                final var image = build3DShapeImage((color, _) -> color);
                graphics.drawImage(image, 0, 0, null);
                return Mode.FILLED_UNSHADED;
            }

            private Image build3DShapeImage(final Shader shader) {
                final var width = getWidth();
                final var height = getHeight();
                final var img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                final var transform = buildTransform();

                // initialize array with extremely far away depths
                final var zBuffer = new double[width * height];
                Arrays.fill(zBuffer, Double.NEGATIVE_INFINITY);

                useTriangles(t -> {
                    var v1 = transform.apply(t.v1());
                    var v2 = transform.apply(t.v2());
                    var v3 = transform.apply(t.v3());

                    final var normBeforeProjection = v2.minus(v1).crossProduct(v3.minus(v1)).normalized();

                    v1 = project(v1);
                    v2 = project(v2);
                    v3 = project(v3);

                    v1 = v1.plus(width / 2.0, height / 2.0, 0);
                    v2 = v2.plus(width / 2.0, height / 2.0, 0);
                    v3 = v3.plus(width / 2.0, height / 2.0, 0);

                    final var angleCos = Math.abs(normBeforeProjection.z());

                    final var minX = (int) max(0, ceil(min(v1.x(), min(v2.x(), v3.x()))));
                    final var maxX = (int) min(width - 1.0, floor(max(v1.x(), max(v2.x(), v3.x()))));
                    final var minY = (int) max(0, ceil(min(v1.y(), min(v2.y(), v3.y()))));
                    final var maxY = (int) min(height - 1.0, floor(max(v1.y(), max(v2.y(), v3.y()))));

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

            private Vertex project(Vertex vertex) {
                final double distance = 400;
                if (vertex.z() >= distance) {
                    return new Vertex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, vertex.z());
                }
                final double scale = distance / (distance - vertex.z());
                return new Vertex(vertex.x() * scale, vertex.y() * scale, vertex.z());
            }

            private Matrix4 buildTransform() {
                final var heading = Math.toRadians(headingSlider.getValue());
                final var pitch = Math.toRadians(pitchSlider.getValue());
                final var rotation = headingTransform(heading).multiply(pitchTransform(pitch));
                return translationTransform(translateX, translateY, translateZ).multiply(rotation);
            }

            private List<Triangle> createShape() {
                final var shape = Sequence.iterate(tetrahedronTriangles, s -> s.flatMap(Triangle::midPointTriangles))
                        .take(inflationSlider.getValue())
                        .last()
                        .map(Triangle::inflate)
                        .toList();
                logger.info("Nr of triangles: {}", shape.size());
                return shape;
            }

            private void updateShape() {
                shape = createShape();
                repaint();
            }

            private void useTriangles(final Consumer<Triangle> consumer) {
                shape.stream().map(t -> t.resizeBy(zoomSlider.getValue())).forEach(consumer);
            }
        };
        final var inputMap = renderPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        final var actionMap = renderPanel.getActionMap();

        final class TranslateAction extends AbstractAction {
            private final transient Runnable onAction;

            TranslateAction(final Runnable onAction) {
                this.onAction = onAction;
            }

            @Override
            public void actionPerformed(final ActionEvent e) {
                onAction.run();
                renderPanel.repaint();
            }
        }
        actionMap.put("up", new TranslateAction(() -> translateY -= 10));
        actionMap.put("down", new TranslateAction(() -> translateY += 10));
        actionMap.put("left", new TranslateAction(() -> translateX -= 10));
        actionMap.put("right", new TranslateAction(() -> translateX += 10));
        actionMap.put("forward", new TranslateAction(() -> translateZ += 10));
        actionMap.put("backward", new TranslateAction(() -> translateZ -= 10));

        inputMap.put(KeyStroke.getKeyStroke("W"), "up");
        inputMap.put(KeyStroke.getKeyStroke("S"), "down");
        inputMap.put(KeyStroke.getKeyStroke("A"), "left");
        inputMap.put(KeyStroke.getKeyStroke("D"), "right");
        inputMap.put(KeyStroke.getKeyStroke("Q"), "forward");
        inputMap.put(KeyStroke.getKeyStroke("E"), "backward");

        headingSlider.addChangeListener(_ -> renderPanel.repaint());
        pitchSlider.addChangeListener(_ -> renderPanel.repaint());
        zoomSlider.addChangeListener(_ -> renderPanel.repaint());
        inflationSlider.addChangeListener(_ -> renderPanel.updateShape());
        shadingSlider.addChangeListener(_ -> renderPanel.repaint());
        modeButton.addActionListener(_ -> {
            shadingSlider.setVisible(modeButton.getMode() == Mode.FILLED_SHADED);
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

    private static Matrix4 translationTransform(double tx, double ty, double tz) {
        return new Matrix4(
                1, 0, 0, tx,
                0, 1, 0, ty,
                0, 0, 1, tz,
                0, 0, 0, 1
        );
    }

    private static Matrix4 pitchTransform(final double pitch) {
        return new Matrix4(
                1.0, 0.0, 0.0, 0.0,
                0.0, Math.cos(pitch), -Math.sin(pitch), 0.0,
                0.0, Math.sin(pitch), Math.cos(pitch), 0.0,
                0.0, 0.0, 0.0, 1.0
        );
    }

    private static Matrix4 headingTransform(final double heading) {
        return new Matrix4(
                Math.cos(heading), 0.0, Math.sin(heading), 0.0,
                0.0, 1.0, 0.0, 0.0,
                -Math.sin(heading), 0.0, Math.cos(heading), 0.0,
                0.0, 0.0, 0.0, 1.0
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
 * Using a 4 by 4 matrix allows rotation without gimble lock. Related: Quaternions.
 *
 * @param values the values of the 4 by 4 matrix.
 */
record Matrix4(double... values) {

    private static final int SLOT_COUNT = 16;

    Matrix4 {
        if (values.length != SLOT_COUNT) {
            throw new IllegalArgumentException("Must have " + SLOT_COUNT + " values! (Had " + values.length + ")");
        }
        values = Arrays.copyOf(values, values.length);
    }

    Matrix4 multiply(final Matrix4 other) {
        final var result = new double[SLOT_COUNT];
        for (var row = 0; row < 4; row++) {
            for (var col = 0; col < 4; col++) {
                for (var i = 0; i < 4; i++) {
                    result[row * 4 + col] += this.values[row * 4 + i] * other.values[i * 4 + col];
                }
            }
        }
        return new Matrix4(result);
    }

    Vertex apply(final Vertex v) {
        final double x = v.x() * values[0] + v.y() * values[1] + v.z() * values[2] + values[3];
        final double y = v.x() * values[4] + v.y() * values[5] + v.z() * values[6] + values[7];
        final double z = v.x() * values[8] + v.y() * values[9] + v.z() * values[10] + values[11];
        final double w = v.x() * values[12] + v.y() * values[13] + v.z() * values[14] + values[15];
        return new Vertex(x / w, y / w, z / w);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Matrix4(double[] otherValues) && Arrays.equals(values, otherValues);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public String toString() {
        return "Matrix4{values=%s}".formatted(Arrays.toString(values));
    }
}
