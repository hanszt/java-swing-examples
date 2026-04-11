package hzt.samples3d;

import org.hzt.utils.sequences.Sequence;

import java.awt.*;
import java.util.List;

public final class Triangles {

    private Triangles() {
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


    public static List<Triangle> getTriangles(final int resolution) {
        return Sequence.iterate(tetrahedronTriangles, s -> s.flatMap(Triangle::midPointTriangles))
                .take(resolution)
                .last()
                .map(Triangle::inflate)
                .toList();
    }
}
