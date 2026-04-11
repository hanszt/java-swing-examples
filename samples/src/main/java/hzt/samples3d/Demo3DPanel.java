package hzt.samples3d;

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

import static hzt.samples3d.Transforms.buildTransform;
import static hzt.samples3d.Triangles.getTriangles;
import static java.lang.Math.*;

class Demo3DPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(Demo3DPanel.class);

    private final JSlider headingSlider;
    private final JSlider pitchSlider;
    private final JSlider zoomSlider;
    private final JSlider shadingSlider;
    private final JSlider inflationSlider;
    private final RenderingModeButton modeButton;

    private double translateX = 0;
    private double translateY = 0;
    private double translateZ = -100;
    private List<Triangle> shape;

    public Demo3DPanel(
            JSlider headingSlider,
            JSlider pitchSlider,
            JSlider zoomSlider,
            JSlider shadingSlider,
            JSlider inflationSlider,
            RenderingModeButton modeButton
    ) {
        this.headingSlider = headingSlider;
        this.pitchSlider = pitchSlider;
        this.zoomSlider = zoomSlider;
        this.shadingSlider = shadingSlider;
        this.inflationSlider = inflationSlider;
        this.modeButton = modeButton;
        this.shape = createShape();
        configureTranslationKeymap();
    }

    private List<Triangle> createShape() {
        final var shape = getTriangles(inflationSlider.getValue());
        logger.info("Nr of triangles: {}", shape.size());
        return shape;
    }

    public void updateShape() {
        shape = createShape();
        repaint();
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        final var mode = switch (modeButton.getMode()) {
            case WIRE_FRAME -> drawWireframe(g);
            case FILLED_SHADED -> drawFilledShaded(g);
            case FILLED_UNSHADED -> drawFilledUnShaded(g);
        };
        logger.trace("Painting with mode: {}", mode);
    }

    private RenderingModeButton.Mode drawWireframe(final Graphics graphics) {
        final var headingDeg = headingSlider.getValue();
        final var pitchDeg = pitchSlider.getValue();
        final var transform = buildTransform(translateX, translateY, translateZ, headingDeg, pitchDeg);

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

            final var path = new Path2D.Double();
            path.moveTo(v1.x(), v1.y());
            path.lineTo(v2.x(), v2.y());
            path.lineTo(v3.x(), v3.y());
            path.closePath();
            g2d.draw(path);
        });
        return RenderingModeButton.Mode.WIRE_FRAME;
    }

    private RenderingModeButton.Mode drawFilledShaded(final Graphics graphics) {
        final var image = build3DShapeImage(this::shade);
        graphics.drawImage(image, 0, 0, null);
        return RenderingModeButton.Mode.FILLED_SHADED;
    }

    private Color shade(final Color color, final double shade) {
        final var exponent = shadingSlider.getValue() / 100.0;
        final var redLinear = pow(color.getRed(), exponent) * shade;
        final var greenLinear = pow(color.getGreen(), exponent) * shade;
        final var blueLinear = pow(color.getBlue(), exponent) * shade;

        final var red = (int) pow(redLinear, 1 / exponent);
        final var green = (int) pow(greenLinear, 1 / exponent);
        final var blue = (int) pow(blueLinear, 1 / exponent);

        return new Color(red, green, blue);
    }

    private RenderingModeButton.Mode drawFilledUnShaded(final Graphics graphics) {
        final var image = build3DShapeImage((color, _) -> color);
        graphics.drawImage(image, 0, 0, null);
        return RenderingModeButton.Mode.FILLED_UNSHADED;
    }

    private Image build3DShapeImage(final Shader shader) {
        final var width = getWidth();
        final var height = getHeight();
        final var img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final var headingDeg = headingSlider.getValue();
        final var pitchDeg = pitchSlider.getValue();
        final var transform = buildTransform(translateX, translateY, translateZ, headingDeg, pitchDeg);

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

            final var angleCos = abs(normBeforeProjection.z());

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
        // distance defines the position of the projection plane relative to the viewer. It's like the screen on which the 3D scene is being projected. Objects
        // further from this plane will appear smaller, and closer objects larger.
        final double distance = 500;
        // Simple clipping logic
        if (vertex.z() >= distance) {
            return new Vertex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, vertex.z());
        }
        final double scale = distance / (distance - vertex.z());
        return new Vertex(vertex.x() * scale, vertex.y() * scale, vertex.z());
    }

    private void useTriangles(final Consumer<Triangle> consumer) {
        shape.stream().map(t -> t.resizeBy(zoomSlider.getValue())).forEach(consumer);
    }

    private void configureTranslationKeymap() {
        final var actionMap = getActionMap();
        actionMap.put("up", new TranslateAction(() -> translateY -= 10));
        actionMap.put("down", new TranslateAction(() -> translateY += 10));
        actionMap.put("left", new TranslateAction(() -> translateX -= 10));
        actionMap.put("right", new TranslateAction(() -> translateX += 10));
        actionMap.put("forward", new TranslateAction(() -> translateZ += 10));
        actionMap.put("backward", new TranslateAction(() -> translateZ -= 10));

        final var inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("W"), "up");
        inputMap.put(KeyStroke.getKeyStroke("S"), "down");
        inputMap.put(KeyStroke.getKeyStroke("A"), "left");
        inputMap.put(KeyStroke.getKeyStroke("D"), "right");
        inputMap.put(KeyStroke.getKeyStroke("Q"), "forward");
        inputMap.put(KeyStroke.getKeyStroke("E"), "backward");
    }

    private final class TranslateAction extends AbstractAction {
        private final transient Runnable onAction;

        TranslateAction(final Runnable onAction) {
            this.onAction = onAction;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            onAction.run();
            repaint();
        }
    }
}
