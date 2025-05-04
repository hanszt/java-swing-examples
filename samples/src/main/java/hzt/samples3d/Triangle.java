package hzt.samples3d;

import org.hzt.utils.sequences.Sequence;

import java.awt.*;

record Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {

    /**
     * Normalizing the vertices projects them on a circle around 0, 0, 0
     */
    Triangle inflate() {
        final var v1n = v1.normalized();
        final var v2n = v2.normalized();
        final var v3n = v3.normalized();
        return new Triangle(v1n, v2n, v3n, color);
    }

    Triangle resizeBy(final double factor) {
        final var v1n = v1.scale(factor);
        final var v2n = v2.scale(factor);
        final var v3n = v3.scale(factor);
        return new Triangle(v1n, v2n, v3n, color);
    }

    /**
     * Multiply by creating smaller inner triangles using the midpoint
     */
    Sequence<Triangle> midPointTriangles() {
        final var m1 = v1.midPoint(v2);
        final var m2 = v2.midPoint(v3);
        final var m3 = v1.midPoint(v3);
        return Sequence.of(
                new Triangle(v1, m1, m3, color),
                new Triangle(v2, m1, m2, color),
                new Triangle(v3, m2, m3, color),
                new Triangle(m1, m2, m3, color)
        );
    }
}
