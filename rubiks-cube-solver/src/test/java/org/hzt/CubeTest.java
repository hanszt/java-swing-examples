package org.hzt;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CubeTest {

    /**
     * Working if:
     * F, B, R, L, S, M, E, D, U
     * S follows F, M follows L, E follows D
     */
    @Test
    void testTurning() {
        final Cube cube = new Cube();
        cube.turn("U");
        final var turning1 = cube.testTurning().toList();
        cube.turn("U'");
        final var turning2 = cube.testTurning().toList();

        assertEquals(List.of(
                "0, 0, 0, Y, U, G, L, O, F, ",
                "0, 0, 1, R, L, G, F, ",
                "0, 0, 2, W, D, R, L, G, F, ",
                "0, 1, 0, Y, U, G, L, ",
                "0, 1, 1, R, L, ",
                "0, 1, 2, R, L, W, D, ",
                "0, 2, 0, Y, U, R, B, G, L, ",
                "0, 2, 1, R, L, B, B, ",
                "0, 2, 2, W, D, R, L, B, B, ",
                "1, 0, 0, Y, U, O, F, ",
                "1, 0, 1, G, F, ",
                "1, 0, 2, W, D, G, F, ",
                "1, 1, 0, Y, U, ",
                "1, 1, 1, A, A, ",
                "1, 1, 2, W, D, ",
                "1, 2, 0, R, B, Y, U, ",
                "1, 2, 1, B, B, ",
                "1, 2, 2, W, D, B, B, ",
                "2, 0, 0, Y, U, B, R, O, F, ",
                "2, 0, 1, G, F, O, R, ",
                "2, 0, 2, W, D, G, F, O, R, ",
                "2, 1, 0, Y, U, B, R, ",
                "2, 1, 1, O, R, ",
                "2, 1, 2, W, D, O, R, ",
                "2, 2, 0, Y, U, R, B, B, R, ",
                "2, 2, 1, B, B, O, R, ",
                "2, 2, 2, W, D, B, B, O, R, "
        ), turning1);
        assertEquals(List.of(
                "0, 0, 0, Y, U, R, L, G, F, ",
                "0, 0, 1, R, L, G, F, ",
                "0, 0, 2, W, D, R, L, G, F, ",
                "0, 1, 0, R, L, Y, U, ",
                "0, 1, 1, R, L, ",
                "0, 1, 2, R, L, W, D, ",
                "0, 2, 0, Y, U, R, L, B, B, ",
                "0, 2, 1, R, L, B, B, ",
                "0, 2, 2, W, D, R, L, B, B, ",
                "1, 0, 0, Y, U, G, F, ",
                "1, 0, 1, G, F, ",
                "1, 0, 2, W, D, G, F, ",
                "1, 1, 0, Y, U, ",
                "1, 1, 1, A, A, ",
                "1, 1, 2, W, D, ",
                "1, 2, 0, Y, U, B, B, ",
                "1, 2, 1, B, B, ",
                "1, 2, 2, W, D, B, B, ",
                "2, 0, 0, Y, U, G, F, O, R, ",
                "2, 0, 1, G, F, O, R, ",
                "2, 0, 2, W, D, G, F, O, R, ",
                "2, 1, 0, Y, U, O, R, ",
                "2, 1, 1, O, R, ",
                "2, 1, 2, W, D, O, R, ",
                "2, 2, 0, Y, U, B, B, O, R, ",
                "2, 2, 1, B, B, O, R, ",
                "2, 2, 2, W, D, B, B, O, R, "

        ), turning2);
    }
}