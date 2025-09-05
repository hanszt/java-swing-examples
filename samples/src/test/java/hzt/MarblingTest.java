package hzt;

import hzt.Marbling.Drop;
import hzt.Marbling.Point2D;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarblingTest {

    @Test
    void testMarbledBy() {
        final var size = 10;
        final var drop = new Drop(10, new Point2D(0, 0), null, size);
        final var updatedDrop = drop.marbledBy(new Drop(10, new Point2D(10, 0), null, size));

        System.out.println("Initial");
        drop.vertices.forEach(System.out::println);
        final var vertices = updatedDrop.vertices;
        System.out.println();
        System.out.println("Marbled");
        vertices.forEach(System.out::println);
        assertThat(vertices).hasSize(drop.vertices.size());
    }

}